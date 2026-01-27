package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.*;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.SessaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SessaoValidation {

    private static final Logger logger = LoggerFactory.getLogger(SessaoValidation.class);

    @Value("${mensagem.cliente.nao.encontrado}")
    private String mensagemClienteNaoEncontrado;

    @Value("${mensagem.filme.nao.encontrado}")
    private String mensagemFilmeNaoEncontrado;

    @Value("${mensagem.sessao.nao.encontrada}")
    private String mensagemSessaoNaoEncontrada;

    @Value("${mensagem.sessao.data.invalida}")
    private String mensagemDataInvalida;

    @Value("${mensagem.sessoes.lista.vazia}")
    private String mensagemListaSessoesVazia;

    @Value("${mensagem.sessao.existente}")
    private String mensagemSessaoExistente;

    private final SessaoRepository sessaoRepository;

    public SessaoValidation(SessaoRepository sessaoRepository) {
        this.sessaoRepository = sessaoRepository;
    }

    public void validarCliente(Optional<ClienteResponseDTO> cliente){
        if (cliente.isEmpty()){
            logger.error("Cliente não encontrado");
            throw new ClienteNaoEncontradoException(mensagemClienteNaoEncontrado);
        }
    }

    public void validarFilme(Optional<FilmeResponseDTO> filme){
        if (filme.isEmpty()){
            logger.error("Filme não encontrado");
            throw new FilmeNaoEncontradoException(mensagemFilmeNaoEncontrado);
        }
    }

    public void validarDataHoraSessao(LocalDateTime dataHoraSessao){
        if (dataHoraSessao.isBefore(LocalDateTime.now())){
            logger.error("Data e hora da sessão inválida: {}", dataHoraSessao);
            throw new DataSessaoInvalidaException(mensagemDataInvalida);
        }
    }

    public void validarExistenciaSessaoMesmoHorarioESala(Sessao sessao){
        boolean existeSessao = sessaoRepository.existsBySalaAndDataHoraSessao(sessao.getSala(), sessao.getDataHoraSessao());
        if (existeSessao){
            logger.error("Essa sessão já existe");
            throw new SessaoExistenteException(mensagemSessaoExistente);
        }
    }

    public void validarSessao(Long idSessao){
        if (!sessaoRepository.existsById(idSessao)){
            logger.error("Sessão não encontrada com id: {}", idSessao);
            throw new SessaoNaoEcontradaException(mensagemSessaoNaoEncontrada);
        }
    }

    public void validarBuscaSessoes(){
        if (sessaoRepository.findAll().isEmpty()){
            logger.error("Nenhuma sessão encontrada");
            throw new ListaSessoesVaziaException(mensagemListaSessoesVazia);
        }
    }
}
