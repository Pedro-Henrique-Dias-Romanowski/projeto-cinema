package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.entity.StatusPagamento;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.exceptions.ValorPagamentoSessaoInvalido;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.SessaoService;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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

    @Value("${mensagem.pagamento.invalido}")
    private String mensagemValorPagamentoInvalido;

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

    public void validarBuscaReserva(UUID idCliente, Reserva reserva){
        if (!reservaRepository.existsById(reserva.getId()) || !reserva.getIdCliente().equals(idCliente)){
            logger.error("Nenhuma reserva encontrada para o cliente com ID: {}", idCliente);
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
    }

    public void validarPagamentoSessao(StatusPagamento statusPagamento){
        Reserva reserva = reservaRepository.findById(statusPagamento.getIdReserva()).orElse(null);
        if  (reserva == null){
            logger.error("Reserva não encontrada para o pagamento");
            throw new ReservaNaoEncontradaException(mensagemReservaNaoEncontrada);
        }
        var sessao = sessaoRepository.findById(reserva.getSessao().getId()).orElseThrow(()
        -> new SessaoNaoEcontradaException(mensagemSessaoNaoEncontrada));
        var valorSessao = sessao.getPreco();
        if (statusPagamento.getValor() < valorSessao){
            logger.error("Valor do pagamento é menor que o valor da sessão");
            throw new ValorPagamentoSessaoInvalido(mensagemValorPagamentoInvalido);
        }
    }
}
