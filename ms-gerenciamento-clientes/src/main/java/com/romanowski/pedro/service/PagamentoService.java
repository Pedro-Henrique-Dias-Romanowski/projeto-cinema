package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.entity.Pagamento;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.ClienteValidation;
import com.romanowski.pedro.service.validation.PagamentoValidation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.util.UUID;

@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final ClienteValidation clienteValidation;
    private final PagamentoValidation pagamentoValidation;
    private final RabbitTemplate rabbitTemplate;
    private final ClienteService clienteService;

    @Value("${ms.sessoes-reservas.indisponivel}")
    private String mensagemErroPagamento;

    public PagamentoService(ClienteValidation clienteValidation, PagamentoValidation pagamentoValidation, RabbitTemplate rabbitTemplate, ClienteService clienteService) {
        this.clienteValidation = clienteValidation;
        this.pagamentoValidation = pagamentoValidation;
        this.rabbitTemplate = rabbitTemplate;
        this.clienteService = clienteService;
    }

    @CircuitBreaker(name = "pagamentoService", fallbackMethod = "fallbackRealizarPagamento")
    @Retry(name = "pagamentoService", fallbackMethod = "fallbackRealizarPagamento")
    @RateLimiter(name = "pagamentoService")
    public void realizarPagamento(UUID idCliente, Long idReserva, Double valor){
        logger.info("Iniciando pagamento para o cliente com id: {} e reserva com id: {}", idCliente, idReserva);
        Cliente cliente = clienteService.buscarClientePorId(idCliente).get();
        clienteValidation.validarBuscaPorCliente(cliente.getId());
        pagamentoValidation.validarExistenciaReserva(idCliente, idReserva);
        pagamentoValidation.validarReservaAtivaOuInativa(idCliente, idReserva);
        pagamentoValidation.validarSaldoCliente(idCliente, valor);
        Pagamento pagamento = Pagamento.builder().
                idCliente(idCliente).
                idReserva(idReserva).
                valor(valor).
                build();
        rabbitTemplate.convertAndSend("pagamentos.ex", "", pagamento);
        cliente.setSaldo(cliente.getSaldo() - valor);
    }



    public void fallbackRealizarPagamento(UUID idCliente, Long idReserva, Throwable t) throws  Exception{
        logger.error("Ocorreu um erro ao processar o pagamento para o cliente com id: {} e reserva com id: {}", idCliente, idReserva);

        throw new ServiceUnavailableException(mensagemErroPagamento);

    }
}
