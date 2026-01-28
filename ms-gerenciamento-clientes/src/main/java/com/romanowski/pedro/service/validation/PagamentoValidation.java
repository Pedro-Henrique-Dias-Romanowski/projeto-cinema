package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.exceptions.ReservaInexistenteException;
import com.romanowski.pedro.exceptions.SaldoInsuficienteException;
import com.romanowski.pedro.feign.ReservaFeignClient;
import com.romanowski.pedro.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PagamentoValidation {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoValidation.class);

    private final ReservaFeignClient reservaFeignClient;

    private final ClienteRepository clienteRepository;

    @Value("${reserva.nao.encontrada}")
    private String mensagemReservaNaoEncontrada;

    @Value("${cliente.saldo.insuficiente}")
    private String mensagemSaldoInsuficiente;

    public PagamentoValidation(ReservaFeignClient reservaFeignClient, ClienteRepository clienteRepository) {
        this.reservaFeignClient = reservaFeignClient;
        this.clienteRepository = clienteRepository;
    }

    public void validarExistenciaReserva(Long idCliente, Long idReserva) {
        ReservaResponseDTO reservaResponseDTO = reservaFeignClient.buscarReservaPorId(idCliente, idReserva);
        if (reservaResponseDTO == null) {
            logger.error("Reserva com id {} para o cliente com id {} não encontrada", idReserva, idCliente);
            throw new ReservaInexistenteException(mensagemReservaNaoEncontrada);
        }
    }

    public void validarSaldoCliente(Long idCliente, Double valor){
        var cliente = clienteRepository.findById(idCliente).orElseThrow();
        if (cliente.getSaldo() < valor){
            logger.error("Saldo insuficiente para o cliente com id {}", idCliente);
            throw new SaldoInsuficienteException(mensagemSaldoInsuficiente);
        }
    }
}
