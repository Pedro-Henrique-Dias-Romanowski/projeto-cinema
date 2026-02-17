package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.*;
import com.romanowski.pedro.repository.SessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SessaoValidation")
class SessaoValidationTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @InjectMocks
    private SessaoValidation sessaoValidation;

    private FilmeResponseDTO filmeResponseDTO;
    private ClienteResponseDTO clienteResponseDTO;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        // Inicializar as mensagens usando ReflectionTestUtils
        ReflectionTestUtils.setField(sessaoValidation, "mensagemClienteNaoEncontrado", "Cliente não encontrado");
        ReflectionTestUtils.setField(sessaoValidation, "mensagemFilmeNaoEncontrado", "Filme não encontrado");
        ReflectionTestUtils.setField(sessaoValidation, "mensagemSessaoNaoEncontrada", "Sessao não encontrada");
        ReflectionTestUtils.setField(sessaoValidation, "mensagemDataInvalida", "A data da sessao deve ser futura");
        ReflectionTestUtils.setField(sessaoValidation, "mensagemListaSessoesVazia", "Nenhuma sessão encontrada");
        ReflectionTestUtils.setField(sessaoValidation, "mensagemSessaoExistente", "Já existe uma sessão para este neste horário e nesta sala");

        // Dados de teste
        filmeResponseDTO = new FilmeResponseDTO(
                1L,
                "Filme Teste",
                120,
                "Ação",
                "Diretor Teste",
                LocalDate.of(2026, 1, 1)
        );

        clienteResponseDTO = new ClienteResponseDTO(
                UUID.randomUUID(),
                "Cliente Teste",
                "cliente@teste.com"
        );

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
    @DisplayName("Deve validar cliente com sucesso quando cliente existe")
    void deveValidarClienteComSucessoQuandoClienteExiste() {
        // Given
        Optional<ClienteResponseDTO> clienteOptional = Optional.of(clienteResponseDTO);

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarCliente(clienteOptional));
    }

    @Test
    @DisplayName("Deve lançar ClienteNaoEncontradoException quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        // Given
        Optional<ClienteResponseDTO> clienteVazio = Optional.empty();

        // When & Then
        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> sessaoValidation.validarCliente(clienteVazio)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar filme com sucesso quando filme existe")
    void deveValidarFilmeComSucessoQuandoFilmeExiste() {
        // Given
        Optional<FilmeResponseDTO> filmeOptional = Optional.of(filmeResponseDTO);

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarFilme(filmeOptional));
    }

    @Test
    @DisplayName("Deve lançar FilmeNaoEncontradoException quando filme não existe")
    void deveLancarExcecaoQuandoFilmeNaoExiste() {
        // Given
        Optional<FilmeResponseDTO> filmeVazio = Optional.empty();

        // When & Then
        FilmeNaoEncontradoException exception = assertThrows(
                FilmeNaoEncontradoException.class,
                () -> sessaoValidation.validarFilme(filmeVazio)
        );

        assertEquals("Filme não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar data e hora da sessão com sucesso quando data é futura")
    void deveValidarDataHoraSessaoComSucessoQuandoDataEFutura() {
        // Given
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarDataHoraSessao(dataFutura));
    }

    @Test
    @DisplayName("Deve lançar DataSessaoInvalidaException quando data é passada")
    void deveLancarExcecaoQuandoDataEPassada() {
        // Given
        LocalDateTime dataPassada = LocalDateTime.now().minusDays(1);

        // When & Then
        DataSessaoInvalidaException exception = assertThrows(
                DataSessaoInvalidaException.class,
                () -> sessaoValidation.validarDataHoraSessao(dataPassada)
        );

        assertEquals("A data da sessao deve ser futura", exception.getMessage());
    }


    @Test
    @DisplayName("Deve validar existência de sessão quando não existe sessão no mesmo horário e sala")
    void deveValidarExistenciaQuandoNaoExisteSessaoNoMesmoHorarioESala() {
        // Given
        when(sessaoRepository.existsBySalaAndDataHoraSessao(anyInt(), any(LocalDateTime.class)))
                .thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarExistenciaSessaoMesmoHorarioESala(sessao));

        verify(sessaoRepository, times(1))
                .existsBySalaAndDataHoraSessao(sessao.getSala(), sessao.getDataHoraSessao());
    }

    @Test
    @DisplayName("Deve lançar SessaoExistenteException quando já existe sessão no mesmo horário e sala")
    void deveLancarExcecaoQuandoJaExisteSessaoNoMesmoHorarioESala() {
        // Given
        when(sessaoRepository.existsBySalaAndDataHoraSessao(anyInt(), any(LocalDateTime.class)))
                .thenReturn(true);

        // When & Then
        SessaoExistenteException exception = assertThrows(
                SessaoExistenteException.class,
                () -> sessaoValidation.validarExistenciaSessaoMesmoHorarioESala(sessao)
        );

        assertEquals("Já existe uma sessão para este neste horário e nesta sala", exception.getMessage());
        verify(sessaoRepository, times(1))
                .existsBySalaAndDataHoraSessao(sessao.getSala(), sessao.getDataHoraSessao());
    }

    @Test
    @DisplayName("Deve validar sessão com sucesso quando sessão existe")
    void deveValidarSessaoComSucessoQuandoSessaoExiste() {
        // Given
        Long sessaoId = 1L;
        when(sessaoRepository.existsById(anyLong())).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarSessao(sessaoId));

        verify(sessaoRepository, times(1)).existsById(sessaoId);
    }

    @Test
    @DisplayName("Deve lançar SessaoNaoEcontradaException quando sessão não existe")
    void deveLancarExcecaoQuandoSessaoNaoExiste() {
        // Given
        Long sessaoId = 999L;
        when(sessaoRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        SessaoNaoEcontradaException exception = assertThrows(
                SessaoNaoEcontradaException.class,
                () -> sessaoValidation.validarSessao(sessaoId)
        );

        assertEquals("Sessao não encontrada", exception.getMessage());
        verify(sessaoRepository, times(1)).existsById(sessaoId);
    }

    @Test
    @DisplayName("Deve validar busca de sessões com sucesso quando existem sessões")
    void deveValidarBuscaSessoesComSucessoQuandoExistemSessoes() {
        // Given
        when(sessaoRepository.findAll()).thenReturn(List.of(sessao));

        // When & Then
        assertDoesNotThrow(() -> sessaoValidation.validarBuscaSessoes());

        verify(sessaoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve lançar ListaSessoesVaziaException quando lista de sessões está vazia")
    void deveLancarExcecaoQuandoListaSessoesEstaVazia() {
        // Given
        when(sessaoRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        ListaSessoesVaziaException exception = assertThrows(
                ListaSessoesVaziaException.class,
                () -> sessaoValidation.validarBuscaSessoes()
        );

        assertEquals("Nenhuma sessão encontrada", exception.getMessage());
        verify(sessaoRepository, times(1)).findAll();
    }
}
