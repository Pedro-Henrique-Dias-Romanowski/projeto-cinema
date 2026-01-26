package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.repository.SessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ReservaValidation")
class ReservaValidationTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @InjectMocks
    private ReservaValidation reservaValidation;

    private Sessao sessao;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        // Inicializar mensagens usando ReflectionTestUtils
        ReflectionTestUtils.setField(reservaValidation, "mensagemSessaoNaoEncontrada", "Sessao não encontrada");
        ReflectionTestUtils.setField(reservaValidation, "mensagemListaReservasVazia", "Lista de reservas vazia");

        // Dados de teste
        sessao = Sessao.builder()
                .id(1L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(true)
                .build();

        reserva = Reserva.builder()
                .id(1L)
                .idCliente(1L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso.")
                .build();
    }

    @Test
    @DisplayName("Deve validar sessão com sucesso quando sessão existe e está ativa")
    void deveValidarSessaoComSucessoQuandoSessaoExisteEEstaAtiva() {
        // Given
        when(sessaoRepository.existsById(anyLong())).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarSessao(sessao));

        verify(sessaoRepository, times(1)).existsById(sessao.getId());
    }

    @Test
    @DisplayName("Deve lançar SessaoNaoEcontradaException quando sessão não existe")
    void deveLancarExcecaoQuandoSessaoNaoExiste() {
        // Given
        when(sessaoRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        SessaoNaoEcontradaException exception = assertThrows(
                SessaoNaoEcontradaException.class,
                () -> reservaValidation.validarSessao(sessao)
        );

        assertEquals("Sessao não encontrada", exception.getMessage());
        verify(sessaoRepository, times(1)).existsById(sessao.getId());
    }

    @Test
    @DisplayName("Deve lançar SessaoNaoEcontradaException quando sessão não está ativa")
    void deveLancarExcecaoQuandoSessaoNaoEstaAtiva() {
        // Given
        Sessao sessaoInativa = Sessao.builder()
                .id(1L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(false) // Sessão inativa
                .build();

        when(sessaoRepository.existsById(anyLong())).thenReturn(true);

        // When & Then
        SessaoNaoEcontradaException exception = assertThrows(
                SessaoNaoEcontradaException.class,
                () -> reservaValidation.validarSessao(sessaoInativa)
        );

        assertEquals("Sessao não encontrada", exception.getMessage());
        verify(sessaoRepository, times(1)).existsById(sessaoInativa.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não existe e não está ativa")
    void deveLancarExcecaoQuandoSessaoNaoExisteENaoEstaAtiva() {
        // Given
        Sessao sessaoInativa = Sessao.builder()
                .id(999L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(false)
                .build();

        when(sessaoRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        SessaoNaoEcontradaException exception = assertThrows(
                SessaoNaoEcontradaException.class,
                () -> reservaValidation.validarSessao(sessaoInativa)
        );

        assertEquals("Sessao não encontrada", exception.getMessage());
        verify(sessaoRepository, times(1)).existsById(sessaoInativa.getId());
    }

    @Test
    @DisplayName("Deve validar sessão com ID específico")
    void deveValidarSessaoComIdEspecifico() {
        // Given
        Long idEspecifico = 12345L;
        Sessao sessaoComIdEspecifico = Sessao.builder()
                .id(idEspecifico)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(true)
                .build();

        when(sessaoRepository.existsById(idEspecifico)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarSessao(sessaoComIdEspecifico));

        verify(sessaoRepository, times(1)).existsById(idEspecifico);
    }

    @Test
    @DisplayName("Deve falhar quando sessão tem ativa null")
    void deveFalharQuandoSessaoTemAtivaNull() {
        // Given
        Sessao sessaoComAtivaNulo = Sessao.builder()
                .id(1L)
                .ativa(null)
                .build();

        when(sessaoRepository.existsById(1L)).thenReturn(true);

        // When & Then - ativa null resultará em NullPointerException ou será tratado como false
        assertThrows(Exception.class,
                () -> reservaValidation.validarSessao(sessaoComAtivaNulo));
    }

    // ================= TESTES PARA VALIDAR LISTAGEM DE RESERVAS =================

    @Test
    @DisplayName("Deve validar lista de reservas com sucesso quando lista contém elementos")
    void deveValidarListaDeReservasComSucessoQuandoListaContemElementos() {
        // Given
        List<Reserva> reservas = List.of(reserva);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarListagemReservas(reservas));
    }

    @Test
    @DisplayName("Deve lançar ListaReservasVaziaException quando lista está vazia")
    void deveLancarExcecaoQuandoListaEstaVazia() {
        // Given
        List<Reserva> reservasVazia = new ArrayList<>();

        // When & Then
        ListaReservasVaziaException exception = assertThrows(
                ListaReservasVaziaException.class,
                () -> reservaValidation.validarListagemReservas(reservasVazia)
        );

        assertEquals("Lista de reservas vazia", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar lista com múltiplas reservas")
    void deveValidarListaComMultiplasReservas() {
        // Given
        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(1L)
                .sessao(sessao)
                .pagamentoConfirmado(true)
                .ativa(true)
                .mensagem("Pagamento confirmado.")
                .build();

        Reserva reserva3 = Reserva.builder()
                .id(3L)
                .idCliente(1L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Aguardando pagamento.")
                .build();

        List<Reserva> reservas = List.of(reserva, reserva2, reserva3);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarListagemReservas(reservas));
    }


    @Test
    @DisplayName("Deve validar lista com reservas ativas e inativas")
    void deveValidarListaComReservasAtivasEInativas() {
        // Given
        Reserva reservaInativa = Reserva.builder()
                .id(2L)
                .idCliente(1L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(false)
                .mensagem("Reserva cancelada.")
                .build();

        List<Reserva> reservas = List.of(reserva, reservaInativa);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarListagemReservas(reservas));
    }

    @Test
    @DisplayName("Deve validar lista com reservas de diferentes clientes")
    void deveValidarListaComReservasDeDiferentesClientes() {
        // Given
        Reserva reservaCliente2 = Reserva.builder()
                .id(2L)
                .idCliente(2L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva cliente 2.")
                .build();

        List<Reserva> reservas = List.of(reserva, reservaCliente2);

        // When & Then
        assertDoesNotThrow(() -> reservaValidation.validarListagemReservas(reservas));
    }


    @Test
    @DisplayName("Deve lançar exceção para lista criada como vazia")
    void deveLancarExcecaoParaListaCriadaComoVazia() {
        // Given
        List<Reserva> reservasVazia = List.of();

        // When & Then
        ListaReservasVaziaException exception = assertThrows(
                ListaReservasVaziaException.class,
                () -> reservaValidation.validarListagemReservas(reservasVazia)
        );

        assertEquals("Lista de reservas vazia", exception.getMessage());
    }
}
