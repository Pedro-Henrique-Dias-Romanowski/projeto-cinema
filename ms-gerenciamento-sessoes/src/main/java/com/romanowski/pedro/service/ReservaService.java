package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.ReservaValidation;
import com.romanowski.pedro.service.validation.SessaoValidation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    private final SessaoRepository sessaoRepository;
    private final SessaoValidation sessaoValidation;
    private final ClienteFeignClient clienteFeignClient;
    private final SessaoService sessaoService;
    private final ReservaValidation reservaValidation;

    @Value("${mensagem.reserva.feita}")
    private String mensagemReservaFeita;

    @Value("${mensagem.reserva.cancelada}")
    private String mensagemReservaCancelada;

    @Value("${mensagem.reserva.inexistente}")
    private String mensagemReservaNaoEncontrada;

    public ReservaService(ReservaRepository reservaRepository, SessaoRepository sessaoRepository, SessaoValidation sessaoValidation, ClienteFeignClient clienteFeignClient, SessaoService sessaoService, ReservaValidation reservaValidation) {
        this.reservaRepository = reservaRepository;
        this.sessaoRepository = sessaoRepository;
        this.sessaoValidation = sessaoValidation;
        this.clienteFeignClient = clienteFeignClient;
        this.sessaoService = sessaoService;
        this.reservaValidation = reservaValidation;
    }


    @Transactional
    public Reserva adicionarReserva(Long idCliente, Long idSessao){
        Optional<ClienteResponseDTO> cliente = clienteFeignClient.obterClientePorId(idCliente);
        Sessao sessao = sessaoRepository.findById(idSessao).get();
        reservaValidation.validarSessao(sessao);
        sessaoValidation.validarCliente(cliente);
        Reserva reserva = Reserva.builder()
                .idCliente(idCliente)
                .sessao(sessao)
                .ativa(true)
                .pagamentoConfirmado(false)
                .mensagem(mensagemReservaFeita)
                .build();
        Reserva reservaSalva = reservaRepository.save(reserva);
        sessaoService.adicionarReservasSessao(reservaSalva);
        return reservaRepository.save(reservaSalva);
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarReservas(Long idCliente){
        Optional<ClienteResponseDTO> cliente = clienteFeignClient.obterClientePorId(idCliente);
        sessaoValidation.validarCliente(cliente);
        List<Reserva> reservas = reservaRepository.findAllByIdCliente(idCliente);
        reservaValidation.validarListagemReservas(reservas);
        return reservas;
    }

    public Reserva buscarReservaPorId(Long idCliente, Long idReserva){
        // todo implementar busca de reserva por id
        return null;
    }

    @Transactional
    public void cancelarReserva(Long idCliente, Long idReserva){
        Optional<ClienteResponseDTO> cliente = clienteFeignClient.obterClientePorId(idCliente);
        Reserva reserva = reservaRepository.getReferenceById(idReserva);
        sessaoValidation.validarCliente(cliente);
        reservaValidation.validarCancelamentoReserva(idCliente, reserva);
        reserva.setAtiva(false);
        reserva.setMensagem(mensagemReservaCancelada);
        reservaRepository.save(reserva);
        sessaoService.removerReservasSessao(reserva);
        }
}
