package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservaValidation {

    private final SessaoRepository sessaoRepository;

    @Value("${mensagem.sessao.nao.encontrada}")
    private String mensagemSessaoNaoEncontrada;

    @Value("${mensagem.reservas.lista.vazia}")
    private String mensagemListaReservasVazia;

    public ReservaValidation(SessaoRepository sessaoRepository) {
        this.sessaoRepository = sessaoRepository;
    }

    public void validarSessao(Sessao sessao){
        if (!sessaoRepository.existsById(sessao.getId()) || !sessao.getAtiva() ){
            throw new SessaoNaoEcontradaException(mensagemSessaoNaoEncontrada);
        }
    }

    public void validarListagemReservas(List<Reserva> reservas){
        if (reservas.isEmpty()){
            throw new ListaReservasVaziaException(mensagemListaReservasVazia);
        }
    }
}
