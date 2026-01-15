package com.romanowski.pedro.service;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.feign.CatalogoFeignClient;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.SessaoValidation;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessaoService {

    private static final Logger logger = LoggerFactory.getLogger(SessaoService.class);

    private final SessaoRepository sessaoRepository;
    private final SessaoValidation sessaoValidation;
    private final CatalogoFeignClient catalogoFeignClient;

    public SessaoService(SessaoRepository sessaoRepository, SessaoValidation sessaoValidation, CatalogoFeignClient catalogoFeignClient) {
        this.sessaoRepository = sessaoRepository;
        this.sessaoValidation = sessaoValidation;
        this.catalogoFeignClient = catalogoFeignClient;
    }


    @Transactional
    public Sessao cadastrarSessao(Sessao sessao){
        try {
            logger.info("Definindo título no ThreadLocal: {}", sessao.getTituloFilme());
            // Define o título no ThreadLocal para ser capturado pelo interceptor
            FeignInterceptor.setTitulo(sessao.getTituloFilme());

            logger.info("Chamando Feign Client para obter filme...");
            Optional<FilmeResponseDTO> filme = catalogoFeignClient.obterFilmePorTitulo();
            sessaoValidation.validarFilme(filme);
            sessaoValidation.validarDataHoraSessao(sessao.getDataHoraSessao());
            sessao.setReservas(List.of());
            sessao.setIdFilme(filme.get().idFilme());
            return sessaoRepository.save(sessao);
        } finally {
            logger.info("Limpando ThreadLocal...");
            // Limpa o ThreadLocal para evitar memory leaks
            FeignInterceptor.clearTitulo();
        }
    }

    public Sessao confirmarReservaSessao(Long id){
        return null;
    }

    public List<Sessao> listarSessoes(){
        return List.of();
    }

    public Optional<Sessao> procurarSessaoPorId(Long id){
        return Optional.empty();
    }

    public void cancelarSessao(Long idSessao){
    }

    public void atualizarReservasSessao(Reserva reserva){
        Sessao sessao = sessaoRepository.findById(reserva.getSessao().getId()).get();
        sessao.getReservas().add(reserva);
        sessaoRepository.save(sessao);
    }
}
