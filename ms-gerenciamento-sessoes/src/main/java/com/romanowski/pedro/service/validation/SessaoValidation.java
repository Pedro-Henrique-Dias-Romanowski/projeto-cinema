package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ClienteNaoEncontradoException;
import com.romanowski.pedro.exceptions.DataSessaoInvalidaException;
import com.romanowski.pedro.exceptions.FilmeNaoEncontradoException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SessaoValidation {

    @Value("${mensagem.cliente.nao.encontrado}")
    private String mensagemClienteNaoEncontrado;

    @Value("${mensagem.filme.nao.encontrado}")
    private String mensagemFilmeNaoEncontrado;

    @Value("${mensagem.sessao.nao.encontrada}")
    private String mensagemSessaoNaoEncontrada;

    @Value("${mensagem.sessao.data.invalida}")
    private String mensagemDataInvalida;

    private final SessaoRepository sessaoRepository;

    public SessaoValidation(SessaoRepository sessaoRepository) {
        this.sessaoRepository = sessaoRepository;
    }

    public void validarCliente(Optional<ClienteResponseDTO> cliente){
        if (cliente.isEmpty()){
            throw new ClienteNaoEncontradoException(mensagemClienteNaoEncontrado);
        }
    }

    public void validarFilme(Optional<FilmeResponseDTO> filme){
        if (filme.isEmpty()){
            throw new FilmeNaoEncontradoException(mensagemFilmeNaoEncontrado);
        }
    }

    public void validarDataHoraSessao(LocalDateTime dataHoraSessao){
        if (dataHoraSessao.isBefore(LocalDateTime.now())){
            throw new DataSessaoInvalidaException(mensagemDataInvalida);
        }
    }

    public void validarSessao(Long idSessao){
        if (!sessaoRepository.existsById(idSessao)){
            throw new SessaoNaoEcontradaException(mensagemSessaoNaoEncontrada);
        }
    }
}
