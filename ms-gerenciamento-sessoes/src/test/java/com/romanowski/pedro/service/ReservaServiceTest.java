
package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.exceptions.ClienteNaoEncontradoException;
import com.romanowski.pedro.exceptions.ListaReservasVaziaException;
import com.romanowski.pedro.exceptions.ReservaNaoEncontradaException;
import com.romanowski.pedro.exceptions.SessaoNaoEcontradaException;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.ReservaRepository;
import com.romanowski.pedro.repository.SessaoRepository;
import com.romanowski.pedro.service.email.EmailService;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservaService reservaService;

    private ClienteResponseDTO clienteResponseDTO;
    private Sessao sessao;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        // Inicializar mensagens usando ReflectionTestUtils
        ReflectionTestUtils.setField(reservaService, "mensagemReservaFeita",
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.");
        ReflectionTestUtils.setField(reservaService, "mensagemReservaCancelada",
                "Reserva cancelada com sucesso.");
        ReflectionTestUtils.setField(reservaService, "mensagemReservaNaoEncontrada",
                "Reserva não encontrada");
        ReflectionTestUtils.setField(reservaService, "mensagemPagamentoConfirmado",
                "Pagamento confirmado com sucesso, aproveite o filme!");
        ReflectionTestUtils.setField(reservaService, "mensagemReservaConfirmadaEmail",
                "Sua reserva foi confirmada, aproveite a sessão! Detalhes da reserva: Id reserva: %s, Nome filme: %s, Data: %s, Sala: %s, Preço: %s");
        ReflectionTestUtils.setField(reservaService, "mensagemReservaCanceladaEmail",
                "Sua reserva foi cancelada com sucesso! Detalhes da reserva: Id reserva: %s, Nome filme: %s, Data: %s, Sala: %s, Preço: %s");
        ReflectionTestUtils.setField(reservaService, "mensagemPagamentoReservaConfirmadoEmail",
                "O pagamento da sua reserva foi confirmado com sucesso! Aproveite a sessão! Detalhes da reserva: Id reserva: %s, Nome filme: %s, Data: %s, Sala: %s, Preço: %s");

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
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

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
        verify(reservaRepository, times(1)).save(any(Reserva.class));
        verify(sessaoService, times(1)).adicionarReservasSessao(any(Reserva.class));
        verify(emailService, times(1)).enviarEmail(any(), any(), any());
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
        verify(sessaoService, never()).adicionarReservasSessao(any());
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
        verify(sessaoService, never()).adicionarReservasSessao(any());
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
    @DisplayName("Deve chamar adicionarReservasSessao após salvar reserva")
    void deveChamarAtualizarReservasSessaoAposSalvarReserva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(sessaoService, times(1)).adicionarReservasSessao(reserva);
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
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(reservaRepository, times(1)).save(reservaCaptor.capture());
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
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente1, idSessao);
        reservaService.adicionarReserva(idCliente2, idSessao);

        // Then
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente1);
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente2);
        verify(sessaoRepository, times(2)).findById(idSessao);
        verify(reservaRepository, times(2)).save(any(Reserva.class));
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
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

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
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
    }

    @Test
    @DisplayName("Deve listar todas as reservas de um cliente com sucesso")
    void deveListarTodasReservasDeUmClienteComSucesso() {
        // Given
        Long idCliente = 1L;

        Sessao sessao2 = Sessao.builder()
                .id(2L)
                .idFilme(2L)
                .tituloFilme("Filme Teste 2")
                .sala(2)
                .preco(45.0)
                .dataHoraSessao(LocalDateTime.of(2026, 2, 21, 18, 0))
                .ativa(true)
                .build();

        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(idCliente)
                .sessao(sessao2)
                .pagamentoConfirmado(true)
                .ativa(true)
                .mensagem("Pagamento confirmado.")
                .build();

        List<Reserva> reservas = List.of(reserva, reserva2);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.findAllByIdCliente(idCliente)).thenReturn(reservas);
        doNothing().when(reservaValidation).validarListagemReservas(any());

        // When
        List<Reserva> resultado = reservaService.listarReservas(idCliente);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        assertEquals(idCliente, resultado.get(0).getIdCliente());
        assertEquals(idCliente, resultado.get(1).getIdCliente());

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaRepository, times(1)).findAllByIdCliente(idCliente);
        verify(reservaValidation, times(1)).validarListagemReservas(reservas);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado ao listar reservas")
    void deveLancarExcecaoQuandoClienteNaoForEncontradoAoListarReservas() {
        // Given
        Long idCliente = 999L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.empty());
        doThrow(new ClienteNaoEncontradoException("Cliente não encontrado"))
                .when(sessaoValidation).validarCliente(Optional.empty());

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            reservaService.listarReservas(idCliente);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoValidation, times(1)).validarCliente(Optional.empty());
        verify(reservaRepository, never()).findAllByIdCliente(anyLong());
        verify(reservaValidation, never()).validarListagemReservas(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista de reservas estiver vazia")
    void deveLancarExcecaoQuandoListaDeReservasEstiverVazia() {
        // Given
        Long idCliente = 1L;
        List<Reserva> reservasVazia = new ArrayList<>();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.findAllByIdCliente(idCliente)).thenReturn(reservasVazia);
        doThrow(new ListaReservasVaziaException("Lista de reservas vazia"))
                .when(reservaValidation).validarListagemReservas(reservasVazia);

        // When & Then
        assertThrows(ListaReservasVaziaException.class, () -> {
            reservaService.listarReservas(idCliente);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaRepository, times(1)).findAllByIdCliente(idCliente);
        verify(reservaValidation, times(1)).validarListagemReservas(reservasVazia);
    }

    @Test
    @DisplayName("Deve listar apenas uma reserva quando cliente possui apenas uma")
    void deveListarApenasUmaReservaQuandoClientePossuiApenaUma() {
        // Given
        Long idCliente = 1L;
        List<Reserva> reservas = List.of(reserva);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.findAllByIdCliente(idCliente)).thenReturn(reservas);
        doNothing().when(reservaValidation).validarListagemReservas(any());

        // When
        List<Reserva> resultado = reservaService.listarReservas(idCliente);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(idCliente, resultado.get(0).getIdCliente());
        assertFalse(resultado.get(0).getPagamentoConfirmado());

        verify(reservaRepository, times(1)).findAllByIdCliente(idCliente);
    }


    @Test
    @DisplayName("Deve validar cliente antes de listar reservas")
    void deveValidarClienteAntesDeListarReservas() {
        // Given
        Long idCliente = 1L;
        List<Reserva> reservas = List.of(reserva);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.findAllByIdCliente(idCliente)).thenReturn(reservas);
        doNothing().when(reservaValidation).validarListagemReservas(any());

        // When
        reservaService.listarReservas(idCliente);

        // Then
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaRepository, times(1)).findAllByIdCliente(idCliente);
    }

    @Test
    @DisplayName("Deve retornar lista com todas as reservas ativas e inativas")
    void deveRetornarListaComTodasReservasAtivasEInativas() {
        // Given
        Long idCliente = 1L;

        Reserva reservaInativa = Reserva.builder()
                .id(2L)
                .idCliente(idCliente)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(false)
                .mensagem("Reserva cancelada.")
                .build();

        List<Reserva> reservas = List.of(reserva, reservaInativa);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.findAllByIdCliente(idCliente)).thenReturn(reservas);
        doNothing().when(reservaValidation).validarListagemReservas(any());

        // When
        List<Reserva> resultado = reservaService.listarReservas(idCliente);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getAtiva());
        assertFalse(resultado.get(1).getAtiva());

        verify(reservaRepository, times(1)).findAllByIdCliente(idCliente);
    }

    @Test
    @DisplayName("Deve buscar uma reserva por ID com sucesso")
    void deveBuscarReservaPorIdComSucesso() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(idCliente, reserva);
        when(reservaRepository.findByIdAndIdCliente(idReserva, idCliente)).thenReturn(Optional.of(reserva));

        // When
        Optional<Reserva> resultado = reservaService.buscarReservaPorId(idCliente, idReserva);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals(1L, resultado.get().getIdCliente());
        assertEquals(sessao, resultado.get().getSessao());
        assertTrue(resultado.get().getAtiva());
        assertFalse(resultado.get().getPagamentoConfirmado());

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaValidation, times(1)).validarBuscaReserva(idCliente, reserva);
        verify(reservaRepository, times(1)).findByIdAndIdCliente(idReserva, idCliente);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado ao buscar reserva")
    void deveLancarExcecaoQuandoClienteNaoForEncontradoAoBuscarReserva() {
        // Given
        Long idCliente = 999L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.empty());
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        doThrow(new ClienteNaoEncontradoException("Cliente não encontrado"))
                .when(sessaoValidation).validarCliente(Optional.empty());

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            reservaService.buscarReservaPorId(idCliente, idReserva);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(sessaoValidation, times(1)).validarCliente(Optional.empty());
        verify(reservaValidation, never()).validarBuscaReserva(anyLong(), any());
        verify(reservaRepository, never()).findByIdAndIdCliente(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não pertence ao cliente ao buscar")
    void deveLancarExcecaoQuandoReservaNaoPertenceAoClienteAoBuscar() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        Reserva reservaOutroCliente = Reserva.builder()
                .id(1L)
                .idCliente(2L) // Cliente diferente
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva de outro cliente.")
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reservaOutroCliente));
        doNothing().when(sessaoValidation).validarCliente(any());
        doThrow(new ReservaNaoEncontradaException("Reserva não encontrada"))
                .when(reservaValidation).validarBuscaReserva(idCliente, reservaOutroCliente);

        // When & Then
        assertThrows(ReservaNaoEncontradaException.class, () -> {
            reservaService.buscarReservaPorId(idCliente, idReserva);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaValidation, times(1)).validarBuscaReserva(idCliente, reservaOutroCliente);
        verify(reservaRepository, never()).findByIdAndIdCliente(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deve buscar reservas de diferentes clientes separadamente")
    void deveBuscarReservasDeDiferentesClientesSeparadamente() {
        // Given
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;
        Long idReserva1 = 1L;
        Long idReserva2 = 2L;

        ClienteResponseDTO cliente2 = new ClienteResponseDTO(2L, "Cliente 2", "cliente2@teste.com");

        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(2L)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente1)).thenReturn(Optional.of(clienteResponseDTO));
        when(clienteFeignClient.obterClientePorId(idCliente2)).thenReturn(Optional.of(cliente2));
        when(reservaRepository.findById(idReserva1)).thenReturn(Optional.ofNullable(reserva));
        when(reservaRepository.findById(idReserva2)).thenReturn(Optional.ofNullable(reserva2));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(anyLong(), any());
        when(reservaRepository.findByIdAndIdCliente(idReserva1, idCliente1)).thenReturn(Optional.of(reserva));
        when(reservaRepository.findByIdAndIdCliente(idReserva2, idCliente2)).thenReturn(Optional.of(reserva2));

        // When
        Optional<Reserva> resultadoCliente1 = reservaService.buscarReservaPorId(idCliente1, idReserva1);
        Optional<Reserva> resultadoCliente2 = reservaService.buscarReservaPorId(idCliente2, idReserva2);

        // Then
        assertTrue(resultadoCliente1.isPresent());
        assertTrue(resultadoCliente2.isPresent());
        assertEquals(1L, resultadoCliente1.get().getIdCliente());
        assertEquals(2L, resultadoCliente2.get().getIdCliente());

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente1);
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente2);
        verify(reservaRepository, times(1)).findByIdAndIdCliente(idReserva1, idCliente1);
        verify(reservaRepository, times(1)).findByIdAndIdCliente(idReserva2, idCliente2);
    }


    @Test
    @DisplayName("Deve cancelar uma reserva com sucesso")
    void deveCancelarReservaComSucesso() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(idCliente, reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).removerReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.cancelarReserva(idCliente, idReserva);

        // Then
        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(sessaoValidation, times(1)).validarCliente(Optional.of(clienteResponseDTO));
        verify(reservaValidation, times(1)).validarBuscaReserva(idCliente, reserva);
        verify(reservaRepository, times(1)).save(reserva);
        verify(sessaoService, times(1)).removerReservasSessao(reserva);
        verify(emailService, times(1)).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Deve atualizar status da reserva ao cancelar")
    void deveAtualizarStatusDaReservaAoCancelar() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(idCliente, reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).removerReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.cancelarReserva(idCliente, idReserva);

        // Then
        assertFalse(reserva.getAtiva());
        assertEquals("Reserva cancelada com sucesso.", reserva.getMensagem());
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado ao cancelar")
    void deveLancarExcecaoQuandoClienteNaoForEncontradoAoCancelar() {
        // Given
        Long idCliente = 999L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.empty());
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doThrow(new ClienteNaoEncontradoException("Cliente não encontrado"))
                .when(sessaoValidation).validarCliente(Optional.empty());

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            reservaService.cancelarReserva(idCliente, idReserva);
        });

        verify(clienteFeignClient, times(1)).obterClientePorId(idCliente);
        verify(reservaRepository, times(1)).findById(idReserva);
        verify(sessaoValidation, times(1)).validarCliente(Optional.empty());
        verify(reservaValidation, never()).validarBuscaReserva(anyLong(), any());
        verify(reservaRepository, never()).save(any());
        verify(sessaoService, never()).removerReservasSessao(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não pertence ao cliente")
    void deveLancarExcecaoQuandoReservaNaoPertenceAoCliente() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        Reserva reservaOutroCliente = Reserva.builder()
                .id(1L)
                .idCliente(2L) // Cliente diferente
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva de outro cliente.")
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reservaOutroCliente));
        doNothing().when(sessaoValidation).validarCliente(any());
        doThrow(new ReservaNaoEncontradaException("Reserva não encontrada"))
                .when(reservaValidation).validarBuscaReserva(idCliente, reservaOutroCliente);

        // When & Then
        assertThrows(ReservaNaoEncontradaException.class, () -> {
            reservaService.cancelarReserva(idCliente, idReserva);
        });

        verify(reservaValidation, times(1)).validarBuscaReserva(idCliente, reservaOutroCliente);
        verify(reservaRepository, never()).save(any());
        verify(sessaoService, never()).removerReservasSessao(any());
    }

    @Test
    @DisplayName("Deve cancelar múltiplas reservas do mesmo cliente")
    void deveCancelarMultiplasReservasDoMesmoCliente() {
        // Given
        Long idCliente = 1L;
        Long idReserva1 = 1L;
        Long idReserva2 = 2L;

        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(idCliente)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva 2.")
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva1)).thenReturn(Optional.of(reserva));
        when(reservaRepository.findById(idReserva2)).thenReturn(Optional.of(reserva2));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(eq(idCliente), any());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(sessaoService).removerReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.cancelarReserva(idCliente, idReserva1);
        reservaService.cancelarReserva(idCliente, idReserva2);

        // Then
        verify(reservaRepository, times(1)).findById(idReserva1);
        verify(reservaRepository, times(1)).findById(idReserva2);
        verify(reservaRepository, times(2)).save(any(Reserva.class));
        verify(sessaoService, times(2)).removerReservasSessao(any(Reserva.class));
    }


    @Test
    @DisplayName("Deve enviar email ao adicionar uma reserva")
    void deveEnviarEmailAoAdicionarReserva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(emailService, times(1)).enviarEmail(
                eq(clienteResponseDTO.emailCliente()),
                eq("Reserva Confirmada"),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve enviar email com os dados corretos da reserva ao adicionar")
    void deveEnviarEmailComDadosCorretosAoAdicionarReserva() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doNothing().when(sessaoValidation).validarCliente(any());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).adicionarReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.adicionarReserva(idCliente, idSessao);

        // Then
        verify(emailService, times(1)).enviarEmail(
                emailCaptor.capture(),
                assuntoCaptor.capture(),
                mensagemCaptor.capture()
        );

        String mensagemEmail = mensagemCaptor.getValue();
        assertEquals("cliente@teste.com", emailCaptor.getValue());
        assertEquals("Reserva Confirmada", assuntoCaptor.getValue());
        assertTrue(mensagemEmail.contains("1")); // ID da reserva
        assertTrue(mensagemEmail.contains("Filme Teste"));
        assertTrue(mensagemEmail.contains("2026-02-20T20:00")); // Data/hora da sessão
        assertTrue(mensagemEmail.contains("1")); // Sala
        assertTrue(mensagemEmail.contains("50.0")); // Preço
    }

    @Test
    @DisplayName("Deve enviar email ao cancelar uma reserva")
    void deveEnviarEmailAoCancelarReserva() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(idCliente, reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).removerReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.cancelarReserva(idCliente, idReserva);

        // Then
        verify(emailService, times(1)).enviarEmail(
                eq(clienteResponseDTO.emailCliente()),
                eq("Cancelamento de reserva"),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve enviar email com os dados corretos da reserva ao cancelar")
    void deveEnviarEmailComDadosCorretosAoCancelarReserva() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(sessaoValidation).validarCliente(any());
        doNothing().when(reservaValidation).validarBuscaReserva(idCliente, reserva);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(sessaoService).removerReservasSessao(any(Reserva.class));
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.cancelarReserva(idCliente, idReserva);

        // Then
        verify(emailService, times(1)).enviarEmail(
                emailCaptor.capture(),
                assuntoCaptor.capture(),
                mensagemCaptor.capture()
        );

        String mensagemEmail = mensagemCaptor.getValue();
        assertEquals("cliente@teste.com", emailCaptor.getValue());
        assertEquals("Cancelamento de reserva", assuntoCaptor.getValue());
        assertTrue(mensagemEmail.contains("1")); // ID da reserva
        assertTrue(mensagemEmail.contains("Filme Teste"));
        assertTrue(mensagemEmail.contains("2026-02-20T20:00")); // Data/hora da sessão
        assertTrue(mensagemEmail.contains("50.0")); // Preço
    }

    @Test
    @DisplayName("Deve enviar email ao confirmar pagamento")
    void deveEnviarEmailAoConfirmarPagamento() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        com.romanowski.pedro.entity.StatusPagamento statusPagamento =
                com.romanowski.pedro.entity.StatusPagamento.builder()
                .idCliente(idCliente)
                .idReserva(idReserva)
                .valor(50.0)
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(reservaValidation).validarPagamentoSessao(statusPagamento);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.verificarFilaPagamento(statusPagamento);

        // Then
        verify(emailService, times(1)).enviarEmail(
                eq(clienteResponseDTO.emailCliente()),
                eq("Pagamento da reserva confirmado"),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve enviar email com os dados corretos ao confirmar pagamento")
    void deveEnviarEmailComDadosCorretosAoConfirmarPagamento() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        com.romanowski.pedro.entity.StatusPagamento statusPagamento =
                com.romanowski.pedro.entity.StatusPagamento.builder()
                .idCliente(idCliente)
                .idReserva(idReserva)
                .valor(50.0)
                .build();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(reservaValidation).validarPagamentoSessao(statusPagamento);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.verificarFilaPagamento(statusPagamento);

        // Then
        verify(emailService, times(1)).enviarEmail(
                emailCaptor.capture(),
                assuntoCaptor.capture(),
                mensagemCaptor.capture()
        );

        String mensagemEmail = mensagemCaptor.getValue();
        assertEquals("cliente@teste.com", emailCaptor.getValue());
        assertEquals("Pagamento da reserva confirmado", assuntoCaptor.getValue());
        assertTrue(mensagemEmail.contains("1")); // ID da reserva
        assertTrue(mensagemEmail.contains("Filme Teste"));
        assertTrue(mensagemEmail.contains("2026-02-20T20:00")); // Data/hora da sessão
        assertTrue(mensagemEmail.contains("50.0")); // Preço
    }

    @Test
    @DisplayName("Deve atualizar status da reserva ao confirmar pagamento")
    void deveAtualizarStatusDaReservaAoConfirmarPagamento() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        com.romanowski.pedro.entity.StatusPagamento statusPagamento =
                com.romanowski.pedro.entity.StatusPagamento.builder()
                .idCliente(idCliente)
                .idReserva(idReserva)
                .valor(50.0)
                .build();

        when(clienteFeignClient.obterClientePorId(idCliente)).thenReturn(Optional.of(clienteResponseDTO));
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.ofNullable(reserva));
        doNothing().when(reservaValidation).validarPagamentoSessao(statusPagamento);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        // When
        reservaService.verificarFilaPagamento(statusPagamento);

        // Then
        assertTrue(reserva.getPagamentoConfirmado());
        assertEquals("Pagamento confirmado com sucesso, aproveite o filme!", reserva.getMensagem());
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    @DisplayName("Não deve enviar email quando validação de pagamento falhar")
    void naoDeveEnviarEmailQuandoValidacaoPagamentoFalhar() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 1L;

        com.romanowski.pedro.entity.StatusPagamento statusPagamento =
                com.romanowski.pedro.entity.StatusPagamento.builder()
                .idCliente(idCliente)
                .idReserva(idReserva)
                .valor(10.0) // Valor inválido
                .build();

        doThrow(new com.romanowski.pedro.exceptions.ValorPagamentoSessaoInvalido("Valor inválido"))
                .when(reservaValidation).validarPagamentoSessao(statusPagamento);

        // When & Then
        assertThrows(com.romanowski.pedro.exceptions.ValorPagamentoSessaoInvalido.class, () -> {
            reservaService.verificarFilaPagamento(statusPagamento);
        });

        verify(emailService, never()).enviarEmail(any(), any(), any());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve enviar email quando reserva não for encontrada ao confirmar pagamento")
    void naoDeveEnviarEmailQuandoReservaNaoForEncontradaAoConfirmarPagamento() {
        // Given
        Long idCliente = 1L;
        Long idReserva = 999L;

        com.romanowski.pedro.entity.StatusPagamento statusPagamento =
                com.romanowski.pedro.entity.StatusPagamento.builder()
                .idCliente(idCliente)
                .idReserva(idReserva)
                .valor(50.0)
                .build();

        doNothing().when(reservaValidation).validarPagamentoSessao(statusPagamento);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.romanowski.pedro.exceptions.ReservaNaoEncontradaException.class, () -> {
            reservaService.verificarFilaPagamento(statusPagamento);
        });

        verify(emailService, never()).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente não for encontrado ao adicionar reserva")
    void naoDeveEnviarEmailQuandoClienteNaoForEncontradoAoAdicionar() {
        // Given
        Long idCliente = 999L;
        Long idSessao = 1L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.empty());
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doNothing().when(reservaValidation).validarSessao(any(Sessao.class));
        doThrow(new com.romanowski.pedro.exceptions.ClienteNaoEncontradoException("Cliente não encontrado"))
                .when(sessaoValidation).validarCliente(any());

        // When & Then
        assertThrows(com.romanowski.pedro.exceptions.ClienteNaoEncontradoException.class, () -> {
            reservaService.adicionarReserva(idCliente, idSessao);
        });

        verify(emailService, never()).enviarEmail(any(), any(), any());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve enviar email quando sessão não for encontrada ao adicionar reserva")
    void naoDeveEnviarEmailQuandoSessaoNaoForEncontradaAoAdicionar() {
        // Given
        Long idCliente = 1L;
        Long idSessao = 999L;

        when(clienteFeignClient.obterClientePorId(anyLong())).thenReturn(Optional.of(clienteResponseDTO));
        when(sessaoRepository.findById(anyLong())).thenReturn(Optional.of(sessao));
        doThrow(new com.romanowski.pedro.exceptions.SessaoNaoEcontradaException("Sessao não encontrada"))
                .when(reservaValidation).validarSessao(any(Sessao.class));

        // When & Then
        assertThrows(com.romanowski.pedro.exceptions.SessaoNaoEcontradaException.class, () -> {
            reservaService.adicionarReserva(idCliente, idSessao);
        });

        verify(emailService, never()).enviarEmail(any(), any(), any());
        verify(reservaRepository, never()).save(any());
    }
}
