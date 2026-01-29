package com.romanowski.pedro.controller;

import com.romanowski.pedro.dto.request.PagamentoRequestDTO;
import com.romanowski.pedro.exceptions.ClienteInexistenteException;
import com.romanowski.pedro.exceptions.ReservaInexistenteException;
import com.romanowski.pedro.exceptions.SaldoInsuficienteException;
import com.romanowski.pedro.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoControllerTest {

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private PagamentoController pagamentoController;

    private PagamentoRequestDTO pagamentoRequestDTO;
    private Long idCliente;
    private Long idReserva;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        idReserva = 100L;
        pagamentoRequestDTO = new PagamentoRequestDTO(50.0);
    }

    @Test
    @DisplayName("Deve realizar pagamento com sucesso e retornar status 200")
    void deveRealizarPagamentoComSucesso() {
        // Arrange
        doNothing().when(pagamentoService).realizarPagamento(idCliente, idReserva, pagamentoRequestDTO.valor());

        // Act
        ResponseEntity<Void> response = pagamentoController.realizarPagamento(idCliente, idReserva, pagamentoRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva, 50.0);
    }

    
    
    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        // Arrange
        String mensagemErro = "Cliente não encontrado";
        doThrow(new ClienteInexistenteException(mensagemErro))
                .when(pagamentoService).realizarPagamento(idCliente, idReserva, pagamentoRequestDTO.valor());

        // Act & Assert
        ClienteInexistenteException exception = assertThrows(
                ClienteInexistenteException.class,
                () -> pagamentoController.realizarPagamento(idCliente, idReserva, pagamentoRequestDTO)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva, 50.0);
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não existe")
    void deveLancarExcecaoQuandoReservaNaoExiste() {
        // Arrange
        String mensagemErro = "Reserva não encontrada";
        doThrow(new ReservaInexistenteException(mensagemErro))
                .when(pagamentoService).realizarPagamento(idCliente, idReserva, pagamentoRequestDTO.valor());

        // Act & Assert
        ReservaInexistenteException exception = assertThrows(
                ReservaInexistenteException.class,
                () -> pagamentoController.realizarPagamento(idCliente, idReserva, pagamentoRequestDTO)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva, 50.0);
    }

    @Test
    @DisplayName("Deve lançar exceção quando saldo é insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        String mensagemErro = "Saldo insuficiente";
        doThrow(new SaldoInsuficienteException(mensagemErro))
                .when(pagamentoService).realizarPagamento(idCliente, idReserva, pagamentoRequestDTO.valor());

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> pagamentoController.realizarPagamento(idCliente, idReserva, pagamentoRequestDTO)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva, 50.0);
    }

    @Test
    @DisplayName("Deve processar múltiplos pagamentos para o mesmo cliente")
    void deveProcessarMultiplosPagamentosParaMesmoCliente() {
        // Arrange
        Long idReserva1 = 100L;
        Long idReserva2 = 101L;
        PagamentoRequestDTO pagamento1 = new PagamentoRequestDTO(30.0);
        PagamentoRequestDTO pagamento2 = new PagamentoRequestDTO(20.0);

        doNothing().when(pagamentoService).realizarPagamento(idCliente, idReserva1, 30.0);
        doNothing().when(pagamentoService).realizarPagamento(idCliente, idReserva2, 20.0);

        // Act
        ResponseEntity<Void> response1 = pagamentoController.realizarPagamento(idCliente, idReserva1, pagamento1);
        ResponseEntity<Void> response2 = pagamentoController.realizarPagamento(idCliente, idReserva2, pagamento2);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva1, 30.0);
        verify(pagamentoService, times(1)).realizarPagamento(idCliente, idReserva2, 20.0);
    }

    @Test
    @DisplayName("Deve processar pagamentos para diferentes clientes")
    void deveProcessarPagamentosParaDiferentesClientes() {
        // Arrange
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;
        Long idReserva1 = 100L;
        Long idReserva2 = 200L;
        PagamentoRequestDTO pagamento = new PagamentoRequestDTO(50.0);

        doNothing().when(pagamentoService).realizarPagamento(idCliente1, idReserva1, 50.0);
        doNothing().when(pagamentoService).realizarPagamento(idCliente2, idReserva2, 50.0);

        // Act
        ResponseEntity<Void> response1 = pagamentoController.realizarPagamento(idCliente1, idReserva1, pagamento);
        ResponseEntity<Void> response2 = pagamentoController.realizarPagamento(idCliente2, idReserva2, pagamento);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(pagamentoService, times(1)).realizarPagamento(idCliente1, idReserva1, 50.0);
        verify(pagamentoService, times(1)).realizarPagamento(idCliente2, idReserva2, 50.0);
    }
}
