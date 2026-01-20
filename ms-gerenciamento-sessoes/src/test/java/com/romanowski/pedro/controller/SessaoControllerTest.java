package com.romanowski.pedro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.mapper.SessaoMapper;
import com.romanowski.pedro.service.SessaoService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SessaoController")
class SessaoControllerTest {

    @Mock
    private SessaoMapper sessaoMapper;

    @Mock
    private SessaoService sessaoService;

    @InjectMocks
    private SessaoController sessaoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SessaoRequestDTO sessaoRequestDTO;
    private Sessao sessao;
    private SessaoResponseDTO sessaoResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sessaoController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Dados de teste
        LocalDateTime dataHoraSessao = LocalDateTime.of(2026, 1, 25, 20, 0);

        sessaoRequestDTO = new SessaoRequestDTO(
                "Filme Teste",
                1,
                50.0,
                dataHoraSessao
        );

        sessao = Sessao.builder()
                .id(1L)
                .idFilme(1L)
                .tituloFilme("Filme Teste")
                .sala(1)
                .preco(50.0)
                .dataHoraSessao(dataHoraSessao)
                .ativa(true)
                .build();

        sessaoResponseDTO = new SessaoResponseDTO(
                1L,
                1,
                "Filme Teste",
                dataHoraSessao,
                true
        );
    }

    @Test
    @DisplayName("Deve cadastrar uma sessão com sucesso")
    void deveCadastrarSessaoComSucesso() throws Exception {
        // Given
        when(sessaoMapper.toEntity(any(SessaoRequestDTO.class))).thenReturn(sessao);
        when(sessaoService.cadastrarSessao(any(Sessao.class))).thenReturn(sessao);
        when(sessaoMapper.toResponseDTO(any(Sessao.class))).thenReturn(sessaoResponseDTO);

        // When & Then
        mockMvc.perform(post("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessaoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSessao").value(1L))
                .andExpect(jsonPath("$.sala").value(1))
                .andExpect(jsonPath("$.tituloFilme").value("Filme Teste"))
                .andExpect(jsonPath("$.ativa").value(true));

        verify(sessaoMapper, times(1)).toEntity(any(SessaoRequestDTO.class));
        verify(sessaoService, times(1)).cadastrarSessao(any(Sessao.class));
        verify(sessaoMapper, times(1)).toResponseDTO(any(Sessao.class));
    }


    @Test
    @DisplayName("Deve listar todas as sessões com sucesso")
    void deveListarSessoesComSucesso() throws Exception {
        // Given
        Sessao sessao2 = Sessao.builder()
                .id(2L)
                .idFilme(2L)
                .tituloFilme("Filme Teste 2")
                .sala(2)
                .preco(45.0)
                .dataHoraSessao(LocalDateTime.of(2026, 1, 26, 18, 0))
                .ativa(true)
                .build();

        SessaoResponseDTO sessaoResponseDTO2 = new SessaoResponseDTO(
                2L,
                2,
                "Filme Teste 2",
                LocalDateTime.of(2026, 1, 26, 18, 0),
                true
        );

        List<Sessao> sessoes = Arrays.asList(sessao, sessao2);

        when(sessaoService.listarSessoes()).thenReturn(sessoes);
        when(sessaoMapper.toResponseDTO(sessao)).thenReturn(sessaoResponseDTO);
        when(sessaoMapper.toResponseDTO(sessao2)).thenReturn(sessaoResponseDTO2);

        // When & Then
        mockMvc.perform(get("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idSessao").value(1L))
                .andExpect(jsonPath("$[0].tituloFilme").value("Filme Teste"))
                .andExpect(jsonPath("$[1].idSessao").value(2L))
                .andExpect(jsonPath("$[1].tituloFilme").value("Filme Teste 2"));

        verify(sessaoService, times(1)).listarSessoes();
        verify(sessaoMapper, times(2)).toResponseDTO(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver sessões")
    void deveRetornarListaVaziaQuandoNaoHouverSessoes() throws Exception {
        // Given
        when(sessaoService.listarSessoes()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(sessaoService, times(1)).listarSessoes();
        verify(sessaoMapper, never()).toResponseDTO(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve procurar sessão por ID com sucesso")
    void deveProcurarSessaoPorIdComSucesso() throws Exception {
        // Given
        Long sessaoId = 1L;
        when(sessaoService.procurarSessaoPorId(anyLong())).thenReturn(Optional.of(sessao));
        when(sessaoMapper.toResponseDTO(any(Sessao.class))).thenReturn(sessaoResponseDTO);

        // When & Then
        mockMvc.perform(get("/v1/sessoes/{id}", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSessao").value(1L))
                .andExpect(jsonPath("$.tituloFilme").value("Filme Teste"))
                .andExpect(jsonPath("$.sala").value(1))
                .andExpect(jsonPath("$.ativa").value(true));

        verify(sessaoService, times(1)).procurarSessaoPorId(sessaoId);
        verify(sessaoMapper, times(1)).toResponseDTO(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve retornar null quando sessão não for encontrada")
    void deveRetornarNullQuandoSessaoNaoForEncontrada() throws Exception {
        // Given
        Long sessaoId = 999L;
        when(sessaoService.procurarSessaoPorId(anyLong())).thenReturn(Optional.empty());
        when(sessaoMapper.toResponseDTO(null)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/v1/sessoes/{id}", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sessaoService, times(1)).procurarSessaoPorId(sessaoId);
        verify(sessaoMapper, times(1)).toResponseDTO(null);
    }

    @Test
    @DisplayName("Deve cancelar sessão com sucesso")
    void deveCancelarSessaoComSucesso() throws Exception {
        // Given
        Long sessaoId = 1L;
        doNothing().when(sessaoService).cancelarSessao(anyLong());

        // When & Then
        mockMvc.perform(delete("/v1/sessoes/{idSessao}", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sessaoService, times(1)).cancelarSessao(sessaoId);
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios no cadastro de sessão")
    void deveValidarCamposObrigatoriosNoCadastro() throws Exception {
        // Given - DTO com campos nulos/inválidos
        SessaoRequestDTO sessaoInvalida = new SessaoRequestDTO(
                null,  // titulo nulo
                null,  // sala nula
                null,  // preco nulo
                null   // data nula
        );

        // When & Then
        mockMvc.perform(post("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessaoInvalida)))
                .andExpect(status().isBadRequest());

        verify(sessaoService, never()).cadastrarSessao(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve validar sala dentro do range permitido")
    void deveValidarSalaDentroDoRange() throws Exception {
        // Given - Sala fora do range (1-5)
        SessaoRequestDTO sessaoSalaInvalida = new SessaoRequestDTO(
                "Filme Teste",
                10,  // sala inválida (max é 5)
                50.0,
                LocalDateTime.of(2026, 1, 25, 20, 0)
        );

        // When & Then
        mockMvc.perform(post("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessaoSalaInvalida)))
                .andExpect(status().isBadRequest());

        verify(sessaoService, never()).cadastrarSessao(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve validar preço dentro do range permitido")
    void deveValidarPrecoDentroDoRange() throws Exception {
        // Given - Preço fora do range (15-70)
        SessaoRequestDTO sessaoPrecoInvalido = new SessaoRequestDTO(
                "Filme Teste",
                1,
                5.0,  // preço abaixo do mínimo (15)
                LocalDateTime.of(2026, 1, 25, 20, 0)
        );

        // When & Then
        mockMvc.perform(post("/v1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessaoPrecoInvalido)))
                .andExpect(status().isBadRequest());

        verify(sessaoService, never()).cadastrarSessao(any(Sessao.class));
    }
}
