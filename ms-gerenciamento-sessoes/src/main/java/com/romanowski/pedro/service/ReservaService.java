package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.ReservaValidation;
import com.romanowski.pedro.service.validation.SessaoValidation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    private final SessaoRepository sessaoRepository;
    private final SessaoValidation sessaoValidation;
    private final ClienteFeignClient clienteFeignClient;
    private final SessaoService sessaoService;

    @Value("${mensagem.reserva.feita}")
    private String mensagemReservaFeita;

    public ReservaService(ReservaRepository reservaRepository, SessaoRepository sessaoRepository, SessaoValidation sessaoValidation, ClienteFeignClient clienteFeignClient, SessaoService sessaoService) {
        this.reservaRepository = reservaRepository;
        this.sessaoRepository = sessaoRepository;
        this.sessaoValidation = sessaoValidation;
        this.clienteFeignClient = clienteFeignClient;
        this.sessaoService = sessaoService;
    }


    @Transactional
    public Reserva adicionarReserva(Long idCliente, Long idSessao){
        Optional<ClienteResponseDTO> cliente = clienteFeignClient.obterClientePorId(idCliente);
        sessaoValidation.validarSessao(idSessao);
        sessaoValidation.validarCliente(cliente);
        Sessao sessao = sessaoRepository.findById(idSessao).get();
        Reserva reserva = Reserva.builder()
                .idCliente(idCliente)
                .sessao(sessao)
                .ativa(true)
                .pagamentoConfirmado(false)
                .mensagem(mensagemReservaFeita)
                .build();
        Reserva reservaSalva = reservaRepository.save(reserva);
        sessaoService.atualizarReservasSessao(reservaSalva);
        return reservaRepository.save(reservaSalva);
    }
}
