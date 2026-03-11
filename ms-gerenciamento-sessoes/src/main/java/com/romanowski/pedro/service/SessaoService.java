package com.romanowski.pedro.service;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.feign.CatalogoFeignClient;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.SessaoValidation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Optional;

@Service
public class SessaoService {

    private static final Logger logger = LoggerFactory.getLogger(SessaoService.class);

    private final SessaoRepository sessaoRepository;
    private final SessaoValidation sessaoValidation;
    private final CatalogoFeignClient catalogoFeignClient;

    @Value("${ms.catalogo.indisponivel}")
    private String mensagemErroCatalogoFeign;

    public SessaoService(SessaoRepository sessaoRepository, SessaoValidation sessaoValidation, CatalogoFeignClient catalogoFeignClient) {
        this.sessaoRepository = sessaoRepository;
        this.sessaoValidation = sessaoValidation;
        this.catalogoFeignClient = catalogoFeignClient;
    }


    @Transactional
    @CircuitBreaker(name = "sessaoService", fallbackMethod = "cadastrarSessaoFalback")
    @Retry(name = "sessaoService", fallbackMethod = "cadastrarSessaoFallback")
    @RateLimiter(name = "sessaoService")
    public Sessao cadastrarSessao(Sessao sessao){
        try {
            logger.info("Iniciando cadastro de sessão para o filme: {}", sessao.getTituloFilme());
            FeignInterceptor.setTitulo(sessao.getTituloFilme());

            Optional<FilmeResponseDTO> filme = catalogoFeignClient.obterFilmePorTitulo();
            sessaoValidation.validarFilme(filme);
            sessaoValidation.validarDataHoraSessao(sessao.getDataHoraSessao());
            sessaoValidation.validarExistenciaSessaoMesmoHorarioESala(sessao);
            sessao.setReservas(List.of());
            sessao.setIdFilme(filme.get().idFilme());
            sessao.setAtiva(true);
            return sessaoRepository.save(sessao);
        } finally {
            FeignInterceptor.clearTitulo();
        }
    }


    @Transactional(readOnly = true)
    @RateLimiter(name = "sessaoService")
    public List<Sessao> listarSessoes(){
        logger.info("Iniciando listagem de sessões");
        sessaoValidation.validarBuscaSessoes();
        return sessaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @RateLimiter(name = "sessaoService")
    public Optional<Sessao> procurarSessaoPorId(Long id){
        logger.info("Iniciando busca de sessão por ID: {}", id);
        sessaoValidation.validarSessao(id);
        return sessaoRepository.findById(id);
    }

    @Transactional
    @RateLimiter(name = "sessaoService")
    public void cancelarSessao(Long idSessao){
        logger.info("Canelando sessão de ID: {}", idSessao);
        sessaoValidation.validarSessao(idSessao);
        Sessao sessao = sessaoRepository.findById(idSessao).get();
        sessao.setAtiva(false);
        sessaoRepository.save(sessao);
    }

    @Transactional
    public void adicionarReservasSessao(Reserva reserva){
        logger.info("Adicionando reserva ID: {} à sessão ID: {}", reserva.getId(), reserva.getSessao().getId());
        Sessao sessao = sessaoRepository.findById(reserva.getSessao().getId()).get();
        sessao.getReservas().add(reserva);
        sessaoRepository.save(sessao);
    }

    @Transactional
    public void removerReservasSessao(Reserva reserva){
        logger.info("Removendo reserva ID: {} da sessão ID: {}", reserva.getId(), reserva.getSessao().getId());
        Sessao sessao = sessaoRepository.findById(reserva.getSessao().getId()).get();
        sessao.getReservas().remove(reserva);
        sessaoRepository.save(sessao);
    }


    public Sessao cadastrarSessaoFalback(Sessao sessao, Throwable throwable) throws Exception{
        logger.error("Ocorreu um erro inesperado no serviço de catálogo ao cadastrar uma sessão com o de id: {}, e na data: {}. Erro: {}", sessao.getId(), sessao.getDataHoraSessao(), throwable.getMessage());
        throw new ServiceUnavailableException(mensagemErroCatalogoFeign);

    }
}
