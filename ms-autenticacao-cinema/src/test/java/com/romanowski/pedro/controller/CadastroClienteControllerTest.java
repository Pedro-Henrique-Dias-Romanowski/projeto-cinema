package com.romanowski.pedro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanowski.pedro.config.SecurityFilter;
import com.romanowski.pedro.controller.handler.GlobalExceptionHandler;
import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaException;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.repository.AdministradorRepository;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.CadastroClienteService;
import com.romanowski.pedro.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CadastroClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes para CadastroClienteController")
@Import({ObjectMapper.class, GlobalExceptionHandler.class})
class CadastroClienteControllerTest {

    private static final String ENDPOINT_CADASTRO = "/v1/auth/clientes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteMapper clienteMapper;

    @MockitoBean
    private CadastroClienteService cadastroClienteService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private AdministradorRepository administradorRepository;

    private ClienteRequestDTO clienteRequestDTO;
    private ClienteEntity clienteEntity;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        clienteRequestDTO = new ClienteRequestDTO(
                "João Silva",
                "joao@example.com",
                "senha123",
                500.0
        );

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(UUID.randomUUID());
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("joao@example.com");
        clienteEntity.setSenha("senhaEncriptada");
        clienteEntity.setSaldo(500.0);
        clienteEntity.setPerfil(Perfil.CLIENTE);

        clienteResponseDTO = new ClienteResponseDTO(
                UUID.randomUUID(),
                "João Silva",
                "joao@example.com"
        );
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class CenariosSucesso {

        @Test
        @DisplayName("Deve cadastrar um cliente com sucesso e retornar HTTP 200")
        void deveCadastrarClienteComSucesso() throws Exception {
            // Arrange
            when(clienteMapper.toEntity(any(ClienteRequestDTO.class))).thenReturn(clienteEntity);
            when(cadastroClienteService.cadastrarCliente(any(ClienteEntity.class))).thenReturn(clienteEntity);
            when(clienteMapper.toResponseDTO(any(ClienteEntity.class))).thenReturn(clienteResponseDTO);

            // Act
            ResultActions response = mockMvc.perform(post(ENDPOINT_CADASTRO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(clienteRequestDTO)));

            // Assert
            response
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome", is("João Silva")))
                    .andExpect(jsonPath("$.email", is("joao@example.com")));

            // Verify
            verify(clienteMapper, times(1)).toEntity(any(ClienteRequestDTO.class));
            verify(cadastroClienteService, times(1)).cadastrarCliente(any(ClienteEntity.class));
            verify(clienteMapper, times(1)).toResponseDTO(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve cadastrar cliente com saldo zero")
        void deveCadastrarClienteComSaldoZero() throws Exception {
            // Arrange
            ClienteRequestDTO requestComSaldoZero = new ClienteRequestDTO(
                    "Maria Santos",
                    "maria@example.com",
                    "senha456",
                    0.0
            );

            ClienteResponseDTO responseDTO = new ClienteResponseDTO(UUID.randomUUID(), "Maria Santos", "maria@example.com");

            when(clienteMapper.toEntity(any(ClienteRequestDTO.class))).thenReturn(clienteEntity);
            when(cadastroClienteService.cadastrarCliente(any(ClienteEntity.class))).thenReturn(clienteEntity);
            when(clienteMapper.toResponseDTO(any(ClienteEntity.class))).thenReturn(responseDTO);

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestComSaldoZero)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome", is("Maria Santos")));
        }
    }

    @Nested
    @DisplayName("Cenários de Erro de Validação")
    class CenariosValidacao {

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando nome está em branco")
        void deveRetornarBadRequestQuandoNomeEmBranco() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "",
                    "joao@example.com",
                    "senha123",
                    500.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando email é inválido")
        void deveRetornarBadRequestQuandoEmailInvalido() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    "email-invalido",
                    "senha123",
                    500.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando senha está em branco")
        void deveRetornarBadRequestQuandoSenhaEmBranco() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    "joao@example.com",
                    "",
                    500.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando saldo é negativo")
        void deveRetornarBadRequestQuandoSaldoNegativo() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    "joao@example.com",
                    "senha123",
                    -100.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando saldo excede o máximo permitido")
        void deveRetornarBadRequestQuandoSaldoExcedeMaximo() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    "joao@example.com",
                    "senha123",
                    1500.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando saldo é nulo")
        void deveRetornarBadRequestQuandoSaldoNulo() throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    "joao@example.com",
                    "senha123",
                    null
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @ParameterizedTest(name = "Deve retornar BAD_REQUEST para email inválido: {0}")
        @MethodSource("emailsInvalidos")
        @DisplayName("Deve retornar BAD_REQUEST para diversos emails inválidos")
        void deveRetornarBadRequestParaEmailsInvalidos(String emailInvalido) throws Exception {
            // Arrange
            ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                    "João Silva",
                    emailInvalido,
                    "senha123",
                    500.0
            );

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestInvalido)))
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        static Stream<Arguments> emailsInvalidos() {
            return Stream.of(
                    Arguments.of("email-sem-arroba"),
                    Arguments.of("@semdominio.com"),
                    Arguments.of("email@"),
                    Arguments.of("")
            );
        }
    }

    @Nested
    @DisplayName("Cenários de Erro de Negócio")
    class CenariosErroNegocio {

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando email já existe")
        void deveRetornarBadRequestQuandoEmailJaExiste() throws Exception {
            // Arrange
            when(clienteMapper.toEntity(any(ClienteRequestDTO.class))).thenReturn(clienteEntity);
            when(cadastroClienteService.cadastrarCliente(any(ClienteEntity.class)))
                    .thenThrow(new EmailExistenteException("Email já cadastrado"));

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, times(1)).cadastrarCliente(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve retornar BAD_REQUEST quando senha é inválida")
        void deveRetornarBadRequestQuandoSenhaInvalida() throws Exception {
            // Arrange
            when(clienteMapper.toEntity(any(ClienteRequestDTO.class))).thenReturn(clienteEntity);
            when(cadastroClienteService.cadastrarCliente(any(ClienteEntity.class)))
                    .thenThrow(new SenhaInvalidaException("Senha deve ter entre 6 e 15 caracteres"));

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, times(1)).cadastrarCliente(any(ClienteEntity.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Erro de Requisição")
    class CenariosErroRequisicao {

        @Test
        @DisplayName("Deve retornar erro quando Content-Type não é JSON")
        void deveRetornarErroQuandoContentTypeNaoEJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("dados invalidos"))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar erro quando corpo da requisição está vazio")
        void deveRetornarErroQuandoCorpoVazio() throws Exception {
            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }

        @Test
        @DisplayName("Deve retornar erro quando JSON é malformado")
        void deveRetornarErroQuandoJsonMalformado() throws Exception {
            // Act & Assert
            mockMvc.perform(post(ENDPOINT_CADASTRO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{nome: 'sem aspas'}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(cadastroClienteService, never()).cadastrarCliente(any());
        }
    }
}

