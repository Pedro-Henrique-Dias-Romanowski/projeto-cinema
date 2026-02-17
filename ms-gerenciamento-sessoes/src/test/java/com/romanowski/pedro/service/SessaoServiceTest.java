package com.romanowski.pedro.service;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.DataSessaoInvalidaException;
import com.romanowski.pedro.exceptions.FilmeNaoEncontradoException;
import com.romanowski.pedro.exceptions.ListaSessoesVaziaException;
import com.romanowski.pedro.exceptions.SessaoExistenteException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.feign.CatalogoFeignClient;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.SessaoValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SessaoService")
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private SessaoValidation sessaoValidation;

    @Mock
    private CatalogoFeignClient catalogoFeignClient;

    @InjectMocks
    private SessaoService sessaoService;

    private Sessao sessao;
    private FilmeResponseDTO filmeResponseDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime dataHoraSessao = LocalDateTime.of(2026, 2, 20, 20, 0);

        filmeResponseDTO = new FilmeResponseDTO(
                1L,
                "Filme Teste",
                120,
                "Ação",
                "Diretor Teste",
                LocalDate.of(2026, 1, 1)
        );

        sessao = Sessao.builder()
                .id(1L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(dataHoraSessao)
                .ativa(true)
                .reservas(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar uma sessão com sucesso")
    void deveCadastrarSessaoComSucesso() {
        // Given
        Sessao sessaoNova = Sessao.builder()
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .build();

        when(catalogoFeignClient.obterFilmePorTitulo()).thenReturn(Optional.of(filmeResponseDTO));
        doNothing().when(sessaoValidation).validarFilme(any());
        doNothing().when(sessaoValidation).validarDataHoraSessao(any());
        doNothing().when(sessaoValidation).validarExistenciaSessaoMesmoHorarioESala(any());
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        try (MockedStatic<FeignInterceptor> mockedStatic = mockStatic(FeignInterceptor.class)) {
            mockedStatic.when(() -> FeignInterceptor.setTitulo(anyString())).then(invocation -> null);
            mockedStatic.when(FeignInterceptor::clearTitulo).then(invocation -> null);

            // When
            Sessao resultado = sessaoService.cadastrarSessao(sessaoNova);

            // Then
            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("Filme Teste", resultado.getTituloFilme());
            assertEquals(1L, resultado.getIdFilme());
            assertTrue(resultado.getAtiva());
            assertNotNull(resultado.getReservas());
            assertTrue(resultado.getReservas().isEmpty());

            verify(catalogoFeignClient, times(1)).obterFilmePorTitulo();
            verify(sessaoValidation, times(1)).validarFilme(any());
            verify(sessaoValidation, times(1)).validarDataHoraSessao(any());
            verify(sessaoValidation, times(1)).validarExistenciaSessaoMesmoHorarioESala(any());
            verify(sessaoRepository, times(1)).save(any(Sessao.class));
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando filme não for encontrado ao cadastrar sessão")
    void deveLancarExcecaoQuandoFilmeNaoForEncontrado() {
        // Given
        when(catalogoFeignClient.obterFilmePorTitulo()).thenReturn(Optional.empty());
        doThrow(new FilmeNaoEncontradoException("Filme não encontrado"))
                .when(sessaoValidation).validarFilme(any());

        try (MockedStatic<FeignInterceptor> mockedStatic = mockStatic(FeignInterceptor.class)) {
            mockedStatic.when(() -> FeignInterceptor.setTitulo(anyString())).then(invocation -> null);
            mockedStatic.when(FeignInterceptor::clearTitulo).then(invocation -> null);

            // When & Then
            assertThrows(FilmeNaoEncontradoException.class, () -> {
                sessaoService.cadastrarSessao(sessao);
            });

            verify(catalogoFeignClient, times(1)).obterFilmePorTitulo();
            verify(sessaoValidation, times(1)).validarFilme(any());
            verify(sessaoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando data da sessão for inválida")
    void deveLancarExcecaoQuandoDataForInvalida() {
        // Given
        Sessao sessaoDataInvalida = Sessao.builder()
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2025, 1, 1, 20, 0))
                .build();

        when(catalogoFeignClient.obterFilmePorTitulo()).thenReturn(Optional.of(filmeResponseDTO));
        doNothing().when(sessaoValidation).validarFilme(any());
        doThrow(new DataSessaoInvalidaException("Data da sessão inválida"))
                .when(sessaoValidation).validarDataHoraSessao(any());

        try (MockedStatic<FeignInterceptor> mockedStatic = mockStatic(FeignInterceptor.class)) {
            mockedStatic.when(() -> FeignInterceptor.setTitulo(anyString())).then(invocation -> null);
            mockedStatic.when(FeignInterceptor::clearTitulo).then(invocation -> null);

            // When & Then
            assertThrows(DataSessaoInvalidaException.class, () -> {
                sessaoService.cadastrarSessao(sessaoDataInvalida);
            });

            verify(sessaoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando já existir sessão no mesmo horário e sala")
    void deveLancarExcecaoQuandoJaExistirSessaoNoMesmoHorarioESala() {
        // Given
        when(catalogoFeignClient.obterFilmePorTitulo()).thenReturn(Optional.of(filmeResponseDTO));
        doNothing().when(sessaoValidation).validarFilme(any());
        doNothing().when(sessaoValidation).validarDataHoraSessao(any());
        doThrow(new SessaoExistenteException("Já existe uma sessão neste horário e sala"))
                .when(sessaoValidation).validarExistenciaSessaoMesmoHorarioESala(any());

        try (MockedStatic<FeignInterceptor> mockedStatic = mockStatic(FeignInterceptor.class)) {
            mockedStatic.when(() -> FeignInterceptor.setTitulo(anyString())).then(invocation -> null);
            mockedStatic.when(FeignInterceptor::clearTitulo).then(invocation -> null);

            // When & Then
            assertThrows(SessaoExistenteException.class, () -> {
                sessaoService.cadastrarSessao(sessao);
            });

            verify(sessaoRepository, never()).save(any());
        }
    }


    @Test
    @DisplayName("Deve listar todas as sessões com sucesso")
    void deveListarTodasSessoesComSucesso() {
        // Given
        Sessao sessao2 = Sessao.builder()
                .id(2L)
                .idFilme(2L)
                .tituloFilme("Filme Teste 2")
                .sala(2)
                .preco(45.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 21, 18, 0))
                .ativa(true)
                .build();

        List<Sessao> sessoes = Arrays.asList(sessao, sessao2);

        doNothing().when(sessaoValidation).validarBuscaSessoes();
        when(sessaoRepository.findAll()).thenReturn(sessoes);

        // When
        List<Sessao> resultado = sessaoService.listarSessoes();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Filme Teste", resultado.get(0).getTituloFilme());
        assertEquals("Filme Teste 2", resultado.get(1).getTituloFilme());

        verify(sessaoValidation, times(1)).validarBuscaSessoes();
        verify(sessaoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista de sessões estiver vazia")
    void deveLancarExcecaoQuandoListaSessoesEstiverVazia() {
        // Given
        doThrow(new ListaSessoesVaziaException("Lista de sessões vazia"))
                .when(sessaoValidation).validarBuscaSessoes();

        // When & Then
        assertThrows(ListaSessoesVaziaException.class, () -> {
            sessaoService.listarSessoes();
        });

        verify(sessaoValidation, times(1)).validarBuscaSessoes();
        verify(sessaoRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve procurar sessão por ID com sucesso")
    void deveProcurarSessaoPorIdComSucesso() {
        // Given
        Long sessaoId = 1L;
        doNothing().when(sessaoValidation).validarSessao(anyLong());
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));

        // When
        Optional<Sessao> resultado = sessaoService.procurarSessaoPorId(sessaoId);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals("Filme Teste", resultado.get().getTituloFilme());

        verify(sessaoValidation, times(1)).validarSessao(sessaoId);
        verify(sessaoRepository, times(1)).findById(sessaoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não for encontrada ao procurar por ID")
    void deveLancarExcecaoQuandoSessaoNaoForEncontradaAoProcurarPorId() {
        // Given
        Long sessaoId = 999L;
        doThrow(new SessaoNaoEcontradaException("Sessão não encontrada"))
                .when(sessaoValidation).validarSessao(anyLong());

        // When & Then
        assertThrows(SessaoNaoEcontradaException.class, () -> {
            sessaoService.procurarSessaoPorId(sessaoId);
        });

        verify(sessaoValidation, times(1)).validarSessao(sessaoId);
        verify(sessaoRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve cancelar sessão com sucesso")
    void deveCancelarSessaoComSucesso() {
        // Given
        Long sessaoId = 1L;
        Sessao sessaoAtiva = Sessao.builder()
                .id(sessaoId)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(true)
                .build();

        doNothing().when(sessaoValidation).validarSessao(anyLong());
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessaoAtiva));
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessaoAtiva);

        // When
        sessaoService.cancelarSessao(sessaoId);

        // Then
        assertFalse(sessaoAtiva.getAtiva());

        verify(sessaoValidation, times(1)).validarSessao(sessaoId);
        verify(sessaoRepository, times(1)).findById(sessaoId);
        verify(sessaoRepository, times(1)).save(sessaoAtiva);
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não existir ao cancelar")
    void deveLancarExcecaoQuandoSessaoNaoExistirAoCancelar() {
        // Given
        Long sessaoId = 999L;
        doThrow(new SessaoNaoEcontradaException("Sessão não encontrada"))
                .when(sessaoValidation).validarSessao(anyLong());

        // When & Then
        assertThrows(SessaoNaoEcontradaException.class, () -> {
            sessaoService.cancelarSessao(sessaoId);
        });

        verify(sessaoValidation, times(1)).validarSessao(sessaoId);
        verify(sessaoRepository, never()).findById(anyLong());
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar reservas da sessão com sucesso")
    void deveAtualizarReservasDaSessaoComSucesso() {
        // Given
        Reserva reserva = Reserva.builder()
                .id(1L)
                .idCliente(UUID.randomUUID())
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .build();

        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        // When
        sessaoService.adicionarReservasSessao(reserva);

        // Then
        assertEquals(1, sessao.getReservas().size());
        assertTrue(sessao.getReservas().contains(reserva));

        verify(sessaoRepository, times(1)).findById(sessao.getId());
        verify(sessaoRepository, times(1)).save(sessao);
    }

    @Test
    @DisplayName("Deve adicionar múltiplas reservas à sessão")
    void deveAdicionarMultiplasReservasASessao() {
        // Given
        Reserva reserva1 = Reserva.builder()
                .id(1L)
                .idCliente(UUID.randomUUID())
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .build();

        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(UUID.randomUUID())
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .build();

        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        // When
        sessaoService.adicionarReservasSessao(reserva1);
        sessaoService.adicionarReservasSessao(reserva2);

        // Then
        assertEquals(2, sessao.getReservas().size());
        assertTrue(sessao.getReservas().contains(reserva1));
        assertTrue(sessao.getReservas().contains(reserva2));

        verify(sessaoRepository, times(2)).findById(sessao.getId());
        verify(sessaoRepository, times(2)).save(sessao);
    }
}
