package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Pagamento;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.ClienteValidation;
import com.romanowski.pedro.service.validation.PagamentoValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final ClienteValidation clienteValidation;
    private final PagamentoValidation pagamentoValidation;
    private final RabbitTemplate rabbitTemplate;

    public PagamentoService(ClienteValidation clienteValidation, PagamentoValidation pagamentoValidation, RabbitTemplate rabbitTemplate) {
        this.clienteValidation = clienteValidation;
        this.pagamentoValidation = pagamentoValidation;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void realizarPagamento(Long idCliente, Long idReserva, Double valor){
        logger.info("Iniciando pagamento para o cliente com id: {} e reserva com id: {}", idCliente, idReserva);
        clienteValidation.validarBuscaPorCliente(idCliente);
        pagamentoValidation.validarExistenciaReserva(idCliente, idReserva);
        pagamentoValidation.validarSaldoCliente(idCliente, valor);
        Pagamento pagamento = Pagamento.builder().
                idCliente(idCliente).
                idReserva(idReserva).
                valor(valor).
                build();
        rabbitTemplate.convertAndSend("pagamentos.ex", "", pagamento);
    }
}
