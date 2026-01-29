package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Pagamento;
import com.romanowski.pedro.exceptions.ClienteInexistenteException;
import com.romanowski.pedro.exceptions.ReservaInativaException;
import com.romanowski.pedro.exceptions.ReservaInexistenteException;
import com.romanowski.pedro.exceptions.SaldoInsuficienteException;
import com.romanowski.pedro.service.validation.ClienteValidation;
import com.romanowski.pedro.service.validation.PagamentoValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private ClienteValidation clienteValidation;

    @Mock
    private PagamentoValidation pagamentoValidation;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Captor
    private ArgumentCaptor<Pagamento> pagamentoCaptor;

    private Long idCliente;
    private Long idReserva;
    private Double valor;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        idReserva = 100L;
        valor = 50.0;
    }

    @Test
    @DisplayName("Deve realizar pagamento com sucesso")
    void deveRealizarPagamentoComSucesso() {
        // Arrange
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);
        doNothing().when(pagamentoValidation).validarSaldoCliente(idCliente, valor);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Pagamento.class));

        // Act
        pagamentoService.realizarPagamento(idCliente, idReserva, valor);

        // Assert
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva);
        verify(pagamentoValidation, times(1)).validarSaldoCliente(idCliente, valor);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("pagamentos.ex"), eq(""), any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve enviar pagamento para fila RabbitMQ com dados corretos")
    void deveEnviarPagamentoParaFilaComDadosCorretos() {
        // Arrange
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);
        doNothing().when(pagamentoValidation).validarSaldoCliente(idCliente, valor);

        // Act
        pagamentoService.realizarPagamento(idCliente, idReserva, valor);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq("pagamentos.ex"), eq(""), pagamentoCaptor.capture());

        Pagamento pagamentoEnviado = pagamentoCaptor.getValue();
        assertNotNull(pagamentoEnviado);
        assertEquals(idCliente, pagamentoEnviado.getIdCliente());
        assertEquals(idReserva, pagamentoEnviado.getIdReserva());
        assertEquals(valor, pagamentoEnviado.getValor());
    }

    
    @Test
    @DisplayName("Deve lançar exceção quando cliente não é encontrado")
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        // Arrange
        String mensagemErro = "Cliente não encontrado";
        doThrow(new ClienteInexistenteException(mensagemErro))
                .when(clienteValidation).validarBuscaPorCliente(idCliente);

        // Act & Assert
        ClienteInexistenteException exception = assertThrows(
                ClienteInexistenteException.class,
                () -> pagamentoService.realizarPagamento(idCliente, idReserva, valor)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, never()).validarExistenciaReserva(anyLong(), anyLong());
        verify(pagamentoValidation, never()).validarReservaAtivaOuInativa(anyLong(), anyLong());
        verify(pagamentoValidation, never()).validarSaldoCliente(anyLong(), anyDouble());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não existe")
    void deveLancarExcecaoQuandoReservaNaoExiste() {
        // Arrange
        String mensagemErro = "Reserva não encontrada";
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doThrow(new ReservaInexistenteException(mensagemErro))
                .when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);

        // Act & Assert
        ReservaInexistenteException exception = assertThrows(
                ReservaInexistenteException.class,
                () -> pagamentoService.realizarPagamento(idCliente, idReserva, valor)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva);
        verify(pagamentoValidation, never()).validarReservaAtivaOuInativa(anyLong(), anyLong());
        verify(pagamentoValidation, never()).validarSaldoCliente(anyLong(), anyDouble());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando saldo é insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        String mensagemErro = "Saldo insuficiente";
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);
        doNothing().when(pagamentoValidation).validarReservaAtivaOuInativa(idCliente, idReserva);
        doThrow(new SaldoInsuficienteException(mensagemErro))
                .when(pagamentoValidation).validarSaldoCliente(idCliente, valor);

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> pagamentoService.realizarPagamento(idCliente, idReserva, valor)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva);
        verify(pagamentoValidation, times(1)).validarReservaAtivaOuInativa(idCliente, idReserva);
        verify(pagamentoValidation, times(1)).validarSaldoCliente(idCliente, valor);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva está inativa")
    void deveLancarExcecaoQuandoReservaInativa() {
        // Arrange
        String mensagemErro = "Reserva inativa";
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);
        doThrow(new ReservaInativaException(mensagemErro))
                .when(pagamentoValidation).validarReservaAtivaOuInativa(idCliente, idReserva);

        // Act & Assert
        ReservaInativaException exception = assertThrows(
                ReservaInativaException.class,
                () -> pagamentoService.realizarPagamento(idCliente, idReserva, valor)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva);
        verify(pagamentoValidation, times(1)).validarReservaAtivaOuInativa(idCliente, idReserva);
        verify(pagamentoValidation, never()).validarSaldoCliente(anyLong(), anyDouble());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }


    @Test
    @DisplayName("Deve processar múltiplos pagamentos sequencialmente")
    void deveProcessarMultiplosPagamentosSequencialmente() {
        // Arrange
        Long idReserva1 = 100L;
        Long idReserva2 = 101L;
        Double valor1 = 30.0;
        Double valor2 = 20.0;

        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(anyLong(), anyLong());
        doNothing().when(pagamentoValidation).validarReservaAtivaOuInativa(anyLong(), anyLong());
        doNothing().when(pagamentoValidation).validarSaldoCliente(anyLong(), anyDouble());

        // Act
        pagamentoService.realizarPagamento(idCliente, idReserva1, valor1);
        pagamentoService.realizarPagamento(idCliente, idReserva2, valor2);

        // Assert
        verify(clienteValidation, times(2)).validarBuscaPorCliente(idCliente);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva1);
        verify(pagamentoValidation, times(1)).validarExistenciaReserva(idCliente, idReserva2);
        verify(pagamentoValidation, times(1)).validarReservaAtivaOuInativa(idCliente, idReserva1);
        verify(pagamentoValidation, times(1)).validarReservaAtivaOuInativa(idCliente, idReserva2);
        verify(pagamentoValidation, times(1)).validarSaldoCliente(idCliente, valor1);
        verify(pagamentoValidation, times(1)).validarSaldoCliente(idCliente, valor2);
        verify(rabbitTemplate, times(2)).convertAndSend(eq("pagamentos.ex"), eq(""), any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve processar pagamentos para diferentes clientes")
    void deveProcessarPagamentosParaDiferentesClientes() {
        // Arrange
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;
        Long idReserva1 = 100L;
        Long idReserva2 = 200L;
        Double valor = 50.0;

        doNothing().when(clienteValidation).validarBuscaPorCliente(anyLong());
        doNothing().when(pagamentoValidation).validarExistenciaReserva(anyLong(), anyLong());
        doNothing().when(pagamentoValidation).validarReservaAtivaOuInativa(anyLong(), anyLong());
        doNothing().when(pagamentoValidation).validarSaldoCliente(anyLong(), anyDouble());

        // Act
        pagamentoService.realizarPagamento(idCliente1, idReserva1, valor);
        pagamentoService.realizarPagamento(idCliente2, idReserva2, valor);

        // Assert
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente1);
        verify(clienteValidation, times(1)).validarBuscaPorCliente(idCliente2);
        verify(rabbitTemplate, times(2)).convertAndSend(eq("pagamentos.ex"), eq(""), any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve enviar pagamento para exchange correto")
    void deveEnviarPagamentoParaExchangeCorreto() {
        // Arrange
        doNothing().when(clienteValidation).validarBuscaPorCliente(idCliente);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idCliente, idReserva);
        doNothing().when(pagamentoValidation).validarReservaAtivaOuInativa(idCliente, idReserva);
        doNothing().when(pagamentoValidation).validarSaldoCliente(idCliente, valor);

        // Act
        pagamentoService.realizarPagamento(idCliente, idReserva, valor);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("pagamentos.ex"),
                eq(""),
                any(Pagamento.class)
        );
    }

    @Test
    @DisplayName("Deve criar objeto Pagamento com todos os dados corretos")
    void deveCriarObjetoPagamentoComTodosDadosCorretos() {
        // Arrange
        Long idClienteEspecifico = 999L;
        Long idReservaEspecifica = 888L;
        Double valorEspecifico = 123.45;

        doNothing().when(clienteValidation).validarBuscaPorCliente(idClienteEspecifico);
        doNothing().when(pagamentoValidation).validarExistenciaReserva(idClienteEspecifico, idReservaEspecifica);
        doNothing().when(pagamentoValidation).validarReservaAtivaOuInativa(idClienteEspecifico, idReservaEspecifica);
        doNothing().when(pagamentoValidation).validarSaldoCliente(idClienteEspecifico, valorEspecifico);

        // Act
        pagamentoService.realizarPagamento(idClienteEspecifico, idReservaEspecifica, valorEspecifico);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq("pagamentos.ex"), eq(""), pagamentoCaptor.capture());
        
        Pagamento pagamento = pagamentoCaptor.getValue();
        assertAll(
                () -> assertNotNull(pagamento, "Pagamento não deve ser nulo"),
                () -> assertEquals(idClienteEspecifico, pagamento.getIdCliente(), "ID do cliente deve estar correto"),
                () -> assertEquals(idReservaEspecifica, pagamento.getIdReserva(), "ID da reserva deve estar correto"),
                () -> assertEquals(valorEspecifico, pagamento.getValor(), "Valor deve estar correto")
        );
    }

    @Test
    @DisplayName("Não deve enviar mensagem para RabbitMQ se validação falhar")
    void naoDeveEnviarMensagemSeValidacaoFalhar() {
        // Arrange
        doThrow(new ClienteInexistenteException("Cliente não encontrado"))
                .when(clienteValidation).validarBuscaPorCliente(idCliente);

        // Act & Assert
        assertThrows(
                ClienteInexistenteException.class,
                () -> pagamentoService.realizarPagamento(idCliente, idReserva, valor)
        );

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }
}
