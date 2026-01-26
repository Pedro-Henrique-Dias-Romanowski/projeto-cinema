package com.romanowski.pedro.controller;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.mapper.ReservaMapper;
import com.romanowski.pedro.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ReservaController")
class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaController reservaController;

    private MockMvc mockMvc;

    private Reserva reserva;
    private ReservaResponseDTO reservaResponseDTO;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservaController).build();

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
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();

        reservaResponseDTO = new ReservaResponseDTO(
                1L,
                1L,
                1L,
                false,
                true,
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."
        );
    }

    @Test
    @DisplayName("Deve criar uma reserva com sucesso")
    void deveCriarReservaComSucesso() throws Exception {
        // Given
        Long idCliente = 1L;
        Long idSessao = 1L;

        when(reservaService.adicionarReserva(anyLong(), anyLong())).thenReturn(reserva);
        when(reservaMapper.toResponseDTO(any(Reserva.class))).thenReturn(reservaResponseDTO);

        // When & Then
        mockMvc.perform(post("/v1/reservas/{idSessao}/{idCliente}", idSessao, idCliente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.idSessao").value(1L))
                .andExpect(jsonPath("$.pagamentoConfirmado").value(false))
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.mensagem").value("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."));

        verify(reservaService, times(1)).adicionarReserva(idCliente, idSessao);
        verify(reservaMapper, times(1)).toResponseDTO(reserva);
    }

    @Test
    @DisplayName("Deve criar reserva para diferentes clientes")
    void deveCriarReservaParaDiferentesClientes() throws Exception {
        // Given
        Long idSessao = 1L;
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;

        Reserva reserva2 = Reserva.builder()
                .id(2L)
                .idCliente(idCliente2)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();

        ReservaResponseDTO reservaResponseDTO2 = new ReservaResponseDTO(
                2L,
                2L,
                1L,
                false,
                true,
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."
        );

        when(reservaService.adicionarReserva(idCliente1, idSessao)).thenReturn(reserva);
        when(reservaService.adicionarReserva(idCliente2, idSessao)).thenReturn(reserva2);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(reservaResponseDTO);
        when(reservaMapper.toResponseDTO(reserva2)).thenReturn(reservaResponseDTO2);

        // When & Then - Cliente 1
        mockMvc.perform(post("/v1/reservas/{idSessao}/{idCliente}", idSessao, idCliente1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.idCliente").value(1L));

        // When & Then - Cliente 2
        mockMvc.perform(post("/v1/reservas/{idSessao}/{idCliente}", idSessao, idCliente2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.idCliente").value(2L));

        verify(reservaService, times(1)).adicionarReserva(idCliente1, idSessao);
        verify(reservaService, times(1)).adicionarReserva(idCliente2, idSessao);
    }

    @Test
    @DisplayName("Deve criar reserva para diferentes sessões")
    void deveCriarReservaParaDiferentesSessoes() throws Exception {
        // Given
        Long idCliente = 1L;
        Long idSessao1 = 1L;
        Long idSessao2 = 2L;

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
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();

        ReservaResponseDTO reservaResponseDTO2 = new ReservaResponseDTO(
                2L,
                1L,
                2L,
                false,
                true,
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."
        );

        when(reservaService.adicionarReserva(idCliente, idSessao1)).thenReturn(reserva);
        when(reservaService.adicionarReserva(idCliente, idSessao2)).thenReturn(reserva2);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(reservaResponseDTO);
        when(reservaMapper.toResponseDTO(reserva2)).thenReturn(reservaResponseDTO2);

        // When & Then - Sessão 1
        mockMvc.perform(post("/v1/reservas/{idSessao}/{idCliente}", idSessao1, idCliente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.idSessao").value(1L));

        // When & Then - Sessão 2
        mockMvc.perform(post("/v1/reservas/{idSessao}/{idCliente}", idSessao2, idCliente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.idSessao").value(2L));

        verify(reservaService, times(1)).adicionarReserva(idCliente, idSessao1);
        verify(reservaService, times(1)).adicionarReserva(idCliente, idSessao2);
    }

    @Test
    @DisplayName("Deve listar todas as reservas de um cliente com sucesso")
    void deveListarReservasDeUmClienteComSucesso() throws Exception {
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

        ReservaResponseDTO reservaResponseDTO2 = new ReservaResponseDTO(
                2L,
                1L,
                2L,
                true,
                true,
                "Pagamento confirmado."
        );

        List<Reserva> reservas = List.of(reserva, reserva2);

        when(reservaService.listarReservas(idCliente)).thenReturn(reservas);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(reservaResponseDTO);
        when(reservaMapper.toResponseDTO(reserva2)).thenReturn(reservaResponseDTO2);

        // When & Then
        mockMvc.perform(get("/v1/reservas/{idCliente}", idCliente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].idCliente").value(1L))
                .andExpect(jsonPath("$[0].idSessao").value(1L))
                .andExpect(jsonPath("$[0].pagamentoConfirmado").value(false))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[0].mensagem").value("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].idCliente").value(1L))
                .andExpect(jsonPath("$[1].idSessao").value(2L))
                .andExpect(jsonPath("$[1].pagamentoConfirmado").value(true))
                .andExpect(jsonPath("$[1].ativa").value(true))
                .andExpect(jsonPath("$[1].mensagem").value("Pagamento confirmado."));

        verify(reservaService, times(1)).listarReservas(idCliente);
        verify(reservaMapper, times(1)).toResponseDTO(reserva);
        verify(reservaMapper, times(1)).toResponseDTO(reserva2);
    }

    @Test
    @DisplayName("Deve listar apenas uma reserva quando cliente possui apenas uma")
    void deveListarApenasUmaReservaQuandoClientePossuiApenaUma() throws Exception {
        // Given
        Long idCliente = 1L;
        List<Reserva> reservas = List.of(reserva);

        when(reservaService.listarReservas(idCliente)).thenReturn(reservas);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(reservaResponseDTO);

        // When & Then
        mockMvc.perform(get("/v1/reservas/{idCliente}", idCliente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].idCliente").value(1L))
                .andExpect(jsonPath("$[0].idSessao").value(1L))
                .andExpect(jsonPath("$[0].pagamentoConfirmado").value(false))
                .andExpect(jsonPath("$[0].ativa").value(true));

        verify(reservaService, times(1)).listarReservas(idCliente);
        verify(reservaMapper, times(1)).toResponseDTO(reserva);
    }

    @Test
    @DisplayName("Deve listar reservas de diferentes clientes separadamente")
    void deveListarReservasDeDiferentesClientesSeparadamente() throws Exception {
        // Given
        Long idCliente1 = 1L;
        Long idCliente2 = 2L;

        Reserva reservaCliente2 = Reserva.builder()
                .id(3L)
                .idCliente(idCliente2)
                .sessao(sessao)
                .pagamentoConfirmado(false)
                .ativa(true)
                .mensagem("Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento.")
                .build();

        ReservaResponseDTO reservaResponseDTOCliente2 = new ReservaResponseDTO(
                3L,
                2L,
                1L,
                false,
                true,
                "Reserva realizada com sucesso. Para confirma-lá, conclua o pagamento."
        );

        List<Reserva> reservasCliente1 = List.of(reserva);
        List<Reserva> reservasCliente2 = List.of(reservaCliente2);

        when(reservaService.listarReservas(idCliente1)).thenReturn(reservasCliente1);
        when(reservaService.listarReservas(idCliente2)).thenReturn(reservasCliente2);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(reservaResponseDTO);
        when(reservaMapper.toResponseDTO(reservaCliente2)).thenReturn(reservaResponseDTOCliente2);

        // When & Then - Cliente 1
        mockMvc.perform(get("/v1/reservas/{idCliente}", idCliente1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].idCliente").value(1L));

        // When & Then - Cliente 2
        mockMvc.perform(get("/v1/reservas/{idCliente}", idCliente2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].idCliente").value(2L));

        verify(reservaService, times(1)).listarReservas(idCliente1);
        verify(reservaService, times(1)).listarReservas(idCliente2);
    }
}
