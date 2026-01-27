package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservaValidation {

    private final SessaoRepository sessaoRepository;

    private final ReservaRepository reservaRepository;

    @Value("${mensagem.sessao.nao.encontrada}")
    private String mensagemSessaoNaoEncontrada;

    @Value("${mensagem.reservas.lista.vazia}")
    private String mensagemListaReservasVazia;

    @Value("${mensagem.reserva.inexistente}")
    private String mensagemReservaNaoEncontrada;

    public ReservaValidation(SessaoRepository sessaoRepository, ReservaRepository reservaRepository) {
        this.sessaoRepository = sessaoRepository;
        this.reservaRepository = reservaRepository;
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

    public void validarReserva(Long idReserva){
        if (reservaRepository.findById(idReserva).isEmpty()){
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
    }

    public void validarCancelamentoReserva(Long idCliente, Reserva reserva){
        if (!reservaRepository.existsById(reserva.getId()) || !reserva.getIdCliente().equals(idCliente)){
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
    }
}
