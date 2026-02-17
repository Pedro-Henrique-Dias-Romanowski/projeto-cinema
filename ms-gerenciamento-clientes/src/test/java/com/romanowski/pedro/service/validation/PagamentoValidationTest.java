package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.ReservaInativaException;
import com.romanowski.pedro.exceptions.ReservaInexistenteException;
import com.romanowski.pedro.exceptions.SaldoInsuficienteException;
import com.romanowski.pedro.feign.ReservaFeignClient;
import com.romanowski.pedro.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PagamentoValidation")
class PagamentoValidationTest {

    @Mock
    private ReservaFeignClient reservaFeignClient;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private PagamentoValidation pagamentoValidation;

    private UUID idCliente;
    private Long idReserva;
    private Double valor;
    private Cliente cliente;
    private ReservaResponseDTO reservaResponseDTO;

    @BeforeEach
    void setUp() {
        // Configurando mensagens injetadas por @Value
        ReflectionTestUtils.setField(pagamentoValidation, "mensagemReservaNaoEncontrada", "Reserva não encontrada");
        ReflectionTestUtils.setField(pagamentoValidation, "mensagemSaldoInsuficiente", "Saldo insuficiente para realizar o pagamento");
        ReflectionTestUtils.setField(pagamentoValidation, "mensagemReservaInativa", "Reserva inativa");

        idCliente = UUID.randomUUID();
        idReserva = 100L;
        valor = 50.0;

        cliente = new Cliente();
        cliente.setId(idCliente);
        cliente.setNome("João Silva");
        cliente.setEmail("joao.silva@email.com");
        cliente.setSenha("senha123");
        cliente.setSaldo(100.0);

        reservaResponseDTO = new ReservaResponseDTO(
                idReserva,
                idCliente,
                1L,
                false,
                true,
                "Reserva realizada com sucesso"
        );
    }

    @Test
    @DisplayName("Deve validar existência da reserva com sucesso")
    void deveValidarExistenciaReservaComSucesso() {
        // Arrange
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(reservaResponseDTO);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva));
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }

    @Test
    @DisplayName("Deve lançar ReservaInexistenteException quando reserva não existe")
    void deveLancarExcecaoQuandoReservaNaoExiste() {
        // Arrange
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(null);

        // Act & Assert
        ReservaInexistenteException exception = assertThrows(
                ReservaInexistenteException.class,
                () -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva)
        );

        assertEquals("Reserva não encontrada", exception.getMessage());
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }

    @Test
    @DisplayName("Deve lançar ReservaInexistenteException quando idCliente da reserva não corresponde")
    void deveLancarExcecaoQuandoIdClienteNaoCorresponde() {
        // Arrange
        UUID outroClienteId = UUID.randomUUID();
        ReservaResponseDTO reservaDeOutroCliente = new ReservaResponseDTO(
                idReserva,
                outroClienteId, // ID de outro cliente
                1L,
                false,
                true,
                "Reserva de outro cliente"
        );
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(reservaDeOutroCliente);

        // Act & Assert
        ReservaInexistenteException exception = assertThrows(
                ReservaInexistenteException.class,
                () -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva)
        );

        assertEquals("Reserva não encontrada", exception.getMessage());
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }


    @Test
    @DisplayName("Deve validar apenas quando idCliente corresponde exatamente")
    void deveValidarApenasQuandoIdClienteCorrespondeExatamente() {
        // Arrange
        UUID idClienteCorreto = UUID.randomUUID();
        ReservaResponseDTO reservaCorreta = new ReservaResponseDTO(
                idReserva,
                idClienteCorreto,
                1L,
                false,
                true,
                "Reserva do cliente correto"
        );
        when(reservaFeignClient.buscarReservaPorId(idClienteCorreto, idReserva)).thenReturn(reservaCorreta);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idClienteCorreto, idReserva));
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idClienteCorreto, idReserva);
    }

    @Test
    @DisplayName("Deve validar reserva para diferentes clientes")
    void deveValidarReservaParaDiferentesClientes() {
        // Arrange
        UUID idCliente1 = UUID.randomUUID();
        UUID idCliente2 = UUID.randomUUID();
        Long idReserva1 = 100L;
        Long idReserva2 = 200L;

        ReservaResponseDTO reserva1 = new ReservaResponseDTO(idReserva1, idCliente1, 1L, false, true, "Reserva 1");
        ReservaResponseDTO reserva2 = new ReservaResponseDTO(idReserva2, idCliente2, 2L, false, true, "Reserva 2");

        when(reservaFeignClient.buscarReservaPorId(idCliente1, idReserva1)).thenReturn(reserva1);
        when(reservaFeignClient.buscarReservaPorId(idCliente2, idReserva2)).thenReturn(reserva2);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente1, idReserva1));
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente2, idReserva2));

        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente1, idReserva1);
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente2, idReserva2);
    }

    @Test
    @DisplayName("Deve lançar exceção quando Feign Client retorna null")
    void deveLancarExcecaoQuandoFeignClientRetornaNulo() {
        // Arrange
        when(reservaFeignClient.buscarReservaPorId(any(UUID.class), anyLong())).thenReturn(null);

        // Act & Assert
        ReservaInexistenteException exception = assertThrows(
                ReservaInexistenteException.class,
                () -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva)
        );

        assertNotNull(exception.getMessage());
        assertEquals("Reserva não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar reserva com pagamento já confirmado")
    void deveValidarReservaComPagamentoConfirmado() {
        // Arrange
        ReservaResponseDTO reservaPaga = new ReservaResponseDTO(
                idReserva,
                idCliente,
                1L,
                true, // pagamento confirmado
                true,
                "Reserva paga"
        );
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(reservaPaga);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva));
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }

    @Test
    @DisplayName("Deve validar múltiplas reservas do mesmo cliente")
    void deveValidarMultiplasReservasMesmoCliente() {
        // Arrange
        Long idReserva1 = 100L;
        Long idReserva2 = 101L;
        ReservaResponseDTO reserva1 = new ReservaResponseDTO(idReserva1, idCliente, 1L, false, true, "Reserva 1");
        ReservaResponseDTO reserva2 = new ReservaResponseDTO(idReserva2, idCliente, 2L, false, true, "Reserva 2");

        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva1)).thenReturn(reserva1);
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva2)).thenReturn(reserva2);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva1));
        assertDoesNotThrow(() -> pagamentoValidation.validarExistenciaReserva(idCliente, idReserva2));

        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva1);
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva2);
    }

    // ==================== Testes para validarReservaAtivaOuInativa ====================
    void deveValidarReservaAtivaComSucesso() {
        // Arrange
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(reservaResponseDTO);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarReservaAtivaOuInativa(idCliente, idReserva));
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }

    @Test
    @DisplayName("Deve lançar ReservaInativaException quando reserva está inativa")
    void deveLancarExcecaoQuandoReservaEstáInativa() {
        // Arrange
        ReservaResponseDTO reservaInativa = new ReservaResponseDTO(
                idReserva,
                idCliente,
                1L,
                false,
                false, // reserva inativa
                "Reserva cancelada"
        );
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva)).thenReturn(reservaInativa);

        // Act & Assert
        ReservaInativaException exception = assertThrows(
                ReservaInativaException.class,
                () -> pagamentoValidation.validarReservaAtivaOuInativa(idCliente, idReserva)
        );

        assertEquals("Reserva inativa", exception.getMessage());
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva);
    }

    @Test
    @DisplayName("Deve validar múltiplas reservas ativas")
    void deveValidarMultiplasReservasAtivas() {
        // Arrange
        Long idReserva1 = 100L;
        Long idReserva2 = 101L;
        ReservaResponseDTO reserva1 = new ReservaResponseDTO(idReserva1, idCliente, 1L, false, true, "Reserva 1");
        ReservaResponseDTO reserva2 = new ReservaResponseDTO(idReserva2, idCliente, 2L, false, true, "Reserva 2");

        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva1)).thenReturn(reserva1);
        when(reservaFeignClient.buscarReservaPorId(idCliente, idReserva2)).thenReturn(reserva2);

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarReservaAtivaOuInativa(idCliente, idReserva1));
        assertDoesNotThrow(() -> pagamentoValidation.validarReservaAtivaOuInativa(idCliente, idReserva2));

        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva1);
        verify(reservaFeignClient, times(1)).buscarReservaPorId(idCliente, idReserva2);
    }

    @Test
    @DisplayName("Deve validar saldo do cliente com sucesso quando saldo é suficiente")
    void deveValidarSaldoClienteComSucesso() {
        // Arrange
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertDoesNotThrow(() -> pagamentoValidation.validarSaldoCliente(idCliente, valor));
        verify(clienteRepository, times(1)).findById(idCliente);
    }


    @Test
    @DisplayName("Deve lançar SaldoInsuficienteException quando saldo é menor que o valor")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        cliente.setSaldo(30.0);
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> pagamentoValidation.validarSaldoCliente(idCliente, valor)
        );

        assertEquals("Saldo insuficiente para realizar o pagamento", exception.getMessage());
        verify(clienteRepository, times(1)).findById(idCliente);
    }


    @Test
    @DisplayName("Deve lançar exceção quando cliente não é encontrado no repository")
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> pagamentoValidation.validarSaldoCliente(idCliente, valor)
        );

        verify(clienteRepository, times(1)).findById(idCliente);
    }


    @Test
    @DisplayName("Deve lançar exceção quando valor é ligeiramente maior que saldo")
    void deveLancarExcecaoQuandoValorLigeiramenteMaior() {
        // Arrange
        cliente.setSaldo(50.00);
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> pagamentoValidation.validarSaldoCliente(idCliente, 50.01)
        );

        assertEquals("Saldo insuficiente para realizar o pagamento", exception.getMessage());
    }
}
