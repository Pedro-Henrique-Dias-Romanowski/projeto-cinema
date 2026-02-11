package com.romanowski.pedro.rabbitlistener;

import com.romanowski.pedro.dto.response.StatusPagamentoResponseDTO;
import com.romanowski.pedro.entity.StatusPagamento;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.exceptions.ValorPagamentoSessaoInvalido;
import com.romanowski.pedro.mapper.ReservaMapper;
import com.romanowski.pedro.service.ReservaService;
import com.romanowski.pedro.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoListener {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoListener.class);

    private final ReservaMapper reservaMapper;
    private final ReservaService reservaService;
    private final EmailService emailService;

    public PagamentoListener(ReservaMapper reservaMapper, ReservaService reservaService, EmailService emailService) {
        this.reservaMapper = reservaMapper;
        this.reservaService = reservaService;
        this.emailService = emailService;
    }

    @RabbitListener(queues = "pagamentos.detalhes")
    public void receberMensagem(StatusPagamentoResponseDTO statusPagamento){
        try {
            StatusPagamento pagamento = reservaMapper.toStatusPagamento(statusPagamento);
            if (pagamento != null){
                logger.info("Recebendo mensagem de pagamento. Dados: idCliente: {}, idReserva: {}, valor: {}",
                    statusPagamento.idCliente(), statusPagamento.idReserva(), statusPagamento.valor());
                reservaService.verificarFilaPagamento(pagamento);
                logger.info("Pagamento processado com sucesso para reserva ID: {}", statusPagamento.idReserva());
            } else {
                logger.error("Erro ao converter mensagem de pagamento: StatusPagamento é nulo");
            }
        } catch (ValorPagamentoSessaoInvalido e) {
            logger.error("Valor do pagamento inválido. IdCliente: {}, IdReserva: {}, Valor: {}. Erro: {}",
                statusPagamento.idCliente(),
                statusPagamento.idReserva(),
                statusPagamento.valor(),
                e.getMessage());
            // Mensagem será descartada (não faz sentido reprocessar com valor inválido)
        } catch (ReservaNaoEncontradaException e) {
            logger.error("Reserva não encontrada. IdCliente: {}, IdReserva: {}, Valor: {}. Erro: {}",
                statusPagamento.idCliente(),
                statusPagamento.idReserva(),
                statusPagamento.valor(),
                e.getMessage());
            // Mensagem será descartada (reserva não existe)
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar mensagem de pagamento. IdCliente: {}, IdReserva: {}, Valor: {}. Erro: {}",
                statusPagamento.idCliente(),
                statusPagamento.idReserva(),
                statusPagamento.valor(),
                e.getMessage(),
                e);
            // Mensagem será enviada para Dead Letter Queue após tentativas de retry
            throw e; // Relança para permitir retry
        }
    }
}
