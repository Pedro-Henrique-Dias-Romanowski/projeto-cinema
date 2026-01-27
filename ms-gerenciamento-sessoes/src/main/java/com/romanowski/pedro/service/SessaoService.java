package com.romanowski.pedro.service;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.feign.CatalogoFeignClient;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.SessaoValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Sessao> listarSessoes(){
        sessaoValidation.validarBuscaSessoes();
        return sessaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Sessao> procurarSessaoPorId(Long id){
        sessaoValidation.validarSessao(id);
        return sessaoRepository.findById(id);
    }

    @Transactional
    public void cancelarSessao(Long idSessao){
        sessaoValidation.validarSessao(idSessao);
        Sessao sessao = sessaoRepository.findById(idSessao).get();
        sessao.setAtiva(false);
        sessaoRepository.save(sessao);
    }

    @Transactional
    public void adicionarReservasSessao(Reserva reserva){
        Sessao sessao = sessaoRepository.findById(reserva.getSessao().getId()).get();
        sessao.getReservas().add(reserva);
        sessaoRepository.save(sessao);
    }

    @Transactional
    public void removerReservasSessao(Reserva reserva){
        Sessao sessao = sessaoRepository.findById(reserva.getSessao().getId()).get();
        sessao.getReservas().remove(reserva);
        sessaoRepository.save(sessao);
    }
}
