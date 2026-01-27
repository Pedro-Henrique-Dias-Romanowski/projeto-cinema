package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.SessaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservaValidation {

    private static final Logger logger = LoggerFactory.getLogger(ReservaValidation.class);

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
            logger.error("Sessao não encontrada");
            throw new SessaoNaoEcontradaException(mensagemSessaoNaoEncontrada);
        }
    }

    public void validarListagemReservas(List<Reserva> reservas){
        if (reservas.isEmpty()){
            logger.error("Nenhuma reserva encontrada na lista");
            throw new ListaReservasVaziaException(mensagemListaReservasVazia);
        }
    }

    public void validarReserva(Long idReserva){
        if (reservaRepository.findById(idReserva).isEmpty()){
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
    }

    public void validarBuscaReserva(Long idCliente, Reserva reserva){
        if (!reservaRepository.existsById(reserva.getId()) || !reserva.getIdCliente().equals(idCliente)){
            logger.error("Nenhuma reserva encontrada para o cliente com ID: {}", idCliente);
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
    }
}
