
package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ClienteNaoEncontradoException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.validation.ReservaValidation;
import com.romanowski.pedro.service.validation.SessaoValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ReservaService")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private SessaoValidation sessaoValidation;

    @Mock
    private ClienteFeignClient clienteFeignClient;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private ReservaValidation reservaValidation;

    @InjectMocks
    private ReservaService reservaService;

    private ClienteResponseDTO clienteResponseDTO;
    private Sessao sessao;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        // Inicializar mensagem usando ReflectionTestUtils
        ReflectionTestUtils.setField(reservaService, "mensagemReservaFeita",
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.");

        // Dados de teste
        clienteResponseDTO = new ClienteResponseDTO(
                1L,
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
                .reservas(new ArrayList<>())
                .build();

        reserva = Reserva.builder()
                .id(1L)
                .idCliente(1L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();
    }

    @Test
    @DisplayName("Deve adicionar uma reserva com sucesso")
    void deveAdicionarReservaComSucesso() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        Reserva resultado = reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getIdCliente());
        assertEquals(sessao, resultado.getSessao());
        assertFalse(resultado.getPagamentoConfirmado());
        assertTrue(resultado.getAtiva());
        assertEquals("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.", resultado.getMensagem());

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoRepository, times(1)).findById(idSessao);
        verify(reservaValidation, times(1)).validarSessao(sessao);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaRepository, times(2)).save(any(Reserva.class));
        verify(sessaoService, times(1)).atualizarReservasSessao(any(Reserva.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado")
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        // Given
        Long idCliente = 999L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.empty());
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doThrow(new ClienteNaoEncontradoException("Cliente não encontrado"))
                .when(sessaoValidation).validarCliente(any());

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            reservaService.adicionarReserva(idCliente, idSessao);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoRepository, times(1)).findById(idSessao);
        verify(reservaValidation, times(1)).validarSessao(sessao);
        verify(sessaoValidation, times(1)).validarCliente(Optional.empty());
        verify(reservaRepository, never()).save(any());
        verify(sessaoService, never()).atualizarReservasSessao(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não for encontrada")
    void deveLancarExcecaoQuandoSessaoNaoForEncontrada() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 999L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doThrow(new SessaoNaoEcontradaException("Sessao não encontrada"))
                .when(reservaValidation).validarSessao(any(Sessao.class));

        // When & Then
        assertThrows(SessaoNaoEcontradaException.class, () -> {
            reservaService.adicionarReserva(idCliente, idSessao);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoRepository, times(1)).findById(idSessao);
        verify(reservaValidation, times(1)).validarSessao(sessao);
        verify(reservaRepository, never()).save(any());
        verify(sessaoService, never()).atualizarReservasSessao(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão não estiver ativa")
    void deveLancarExcecaoQuandoSessaoNaoEstiverAtiva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        Sessao sessaoInativa = Sessao.builder()
                .id(1L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 20, 20, 0))
                .ativa(false) // Sessão inativa
                .build();

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessaoInativa));
        doThrow(new SessaoNaoEcontradaException("Sessao não encontrada"))
                .when(reservaValidation).validarSessao(any(Sessao.class));

        // When & Then
        assertThrows(SessaoNaoEcontradaException.class, () -> {
            reservaService.adicionarReserva(idCliente, idSessao);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoRepository, times(1)).findById(idSessao);
        verify(reservaValidation, times(1)).validarSessao(sessaoInativa);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve chamar atualizarReservasSessao após salvar reserva")
    void deveChamarAtualizarReservasSessaoAposSalvarReserva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(sessaoService, times(1)).atualizarReservasSessao(reserva);
    }

    @Test
    @DisplayName("Deve associar sessão correta à reserva")
    void deveAssociarSessaoCorretaAReserva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        ArgumentCaptor<Reserva> reservaCaptor = ArgumentCaptor.forClass(Reserva.class);

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(reservaRepository, times(2)).save(reservaCaptor.capture());
        Reserva reservaCapturada = reservaCaptor.getAllValues().get(0);

        assertEquals(sessao, reservaCapturada.getSessao());
    }

    @Test
    @DisplayName("Deve criar múltiplas reservas para diferentes clientes na mesma sessão")
    void deveCriarMultiplasReservasParaDiferentesClientesNaMesmaSessao() {
        // Given
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;
        Long idSessao = 1L;

        ClienteResponseDTO cliente2 = new ClienteResponseDTO(2L, "Cliente 2", "cliente2@teste.com");

        when(clienteFeignClient.obterClientePorId(idCliente1)).thenReturn(Optional.of(clienteResponseDTO));
        when(clienteFeignClient.obterClientePorId(idCliente2)).thenReturn(Optional.of(cliente2));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        reservaService.adicionarReserva(idCliente1, idSessao);
        reservaService.adicionarReserva(idCliente2, idSessao);

        // Then
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente1);
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente2);
        verify(sessaoRepository, times(2)).findById(idSessao);
        verify(reservaRepository, times(4)).save(any(Reserva.class)); // 2 vezes para cada reserva
    }

    @Test
    @DisplayName("Deve buscar sessão por ID específico")
    void deveBuscarSessaoPorIdEspecifico() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 999L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(idSessao)).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(sessaoRepository, times(1)).findById(idSessao);
    }

    @Test
    @DisplayName("Deve buscar cliente por ID específico")
    void deveBuscarClientePorIdEspecifico() {
        // Given
        Long idCliente = 888L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).atualizarReservasSessao(any(Reserva.class));

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
    }
}
