package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Sessao;
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

    @BeforeEach
    void setUp() {
        // Inicializar mensagem usando ReflectionTestUtils
        ReflectionTestUtils.setField(reservaValidation, "mensagemSessaoNaoEncontrada", "Sessao não encontrada");

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
}
