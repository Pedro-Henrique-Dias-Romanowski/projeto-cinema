package com.romanowski.pedro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanowski.pedro.config.SecurityFilter;
import com.romanowski.pedro.controller.handler.GlobalExceptionHandler;
import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import com.romanowski.pedro.entity.AdministradorEntity;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.repository.AdministradorRepository;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.LoginService;
import com.romanowski.pedro.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes para LoginController")
@Import({ObjectMapper.class, GlobalExceptionHandler.class})
class LoginControllerTest {

    private static final String ENDPOINT_LOGIN_CLIENTE = "/v1/auth/clientes/login";
    private static final String ENDPOINT_LOGIN_ADMIN = "/v1/auth/administradores/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private AdministradorRepository administradorRepository;

    private LoginRequestDTO loginRequestDTO;
    private ClienteEntity clienteEntity;
    private AdministradorEntity administradorEntity;
    private Authentication clienteAuthentication;
    private Authentication adminAuthentication;

    @BeforeEach
    void setUp() {
        loginRequestDTO = new LoginRequestDTO(
                "usuario@example.com",
                "senha123"
        );

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(UUID.randomUUID());
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("usuario@example.com");
        clienteEntity.setSenha("senhaEncriptada");
        clienteEntity.setSaldo(500.0);
        clienteEntity.setPerfil(Perfil.CLIENTE);

        administradorEntity = new AdministradorEntity();
        administradorEntity.setId(UUID.randomUUID());
        administradorEntity.setNome("Admin Silva");
        administradorEntity.setEmail("admin@example.com");
        administradorEntity.setSenha("senhaEncriptada");
        administradorEntity.setPerfil(Perfil.ADMIN);

        clienteAuthentication = mock(Authentication.class);
        when(clienteAuthentication.getPrincipal()).thenReturn(clienteEntity);

        adminAuthentication = mock(Authentication.class);
        when(adminAuthentication.getPrincipal()).thenReturn(administradorEntity);
    }

    @Nested
    @DisplayName("Testes de Login de Cliente")
    class LoginClienteTestes {

        @Test
        @DisplayName("Deve efetuar login de cliente com sucesso e retornar token")
        void deveEfetuarLoginClienteComSucesso() throws Exception {
            // Arrange
            String tokenGerado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.token";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(clienteAuthentication);
            when(loginService.gerarTokenCliente(any(ClienteEntity.class)))
                    .thenReturn(tokenGerado);

            // Act
            ResultActions response = mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequestDTO)));

            // Assert
            response
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", is(tokenGerado)))
                    .andExpect(jsonPath("$.dataHoraLogin", notNullValue()))
                    .andExpect(jsonPath("$.perfilAtribuido", is("CLIENTE")))
                    .andExpect(jsonPath("$.id").exists());

            // Verify
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(loginService, times(1)).gerarTokenCliente(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve retornar erro 401 quando credenciais do cliente são inválidas")
        void deveRetornarErroQuandoCredenciaisClienteInvalidas() throws Exception {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("Credenciais inválidas")));

            // Verify
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(loginService, never()).gerarTokenCliente(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve retornar erro 400 quando email está vazio")
        void deveRetornarErroQuandoEmailVazio() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("", "senha123");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve retornar erro 400 quando senha está vazia")
        void deveRetornarErroQuandoSenhaVazia() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("usuario@example.com", "");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando corpo da requisição está vazio")
        void deveRetornarErroQuandoCorpoVazio() throws Exception {
            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("Testes de Login de Administrador")
    class LoginAdministradorTestes {

        @Test
        @DisplayName("Deve efetuar login de administrador com sucesso e retornar token")
        void deveEfetuarLoginAdministradorComSucesso() throws Exception {
            // Arrange
            String tokenGerado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.adminToken";
            LoginRequestDTO adminLoginDTO = new LoginRequestDTO("admin@example.com", "senha123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(adminAuthentication);
            when(loginService.gerarTokenAdministrador(any(AdministradorEntity.class)))
                    .thenReturn(tokenGerado);

            // Act
            ResultActions response = mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminLoginDTO)));

            // Assert
            response
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", is(tokenGerado)))
                    .andExpect(jsonPath("$.dataHoraLogin", notNullValue()))
                    .andExpect(jsonPath("$.perfilAtribuido", is("ADMIN")))
                    .andExpect(jsonPath("$.id").exists());

            // Verify
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(loginService, times(1)).gerarTokenAdministrador(any(AdministradorEntity.class));
        }

        @Test
        @DisplayName("Deve retornar erro 401 quando credenciais do administrador são inválidas")
        void deveRetornarErroQuandoCredenciaisAdminInvalidas() throws Exception {
            // Arrange
            LoginRequestDTO adminLoginDTO = new LoginRequestDTO("admin@example.com", "senhaErrada");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adminLoginDTO)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("Credenciais inválidas")));

            // Verify
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(loginService, never()).gerarTokenAdministrador(any(AdministradorEntity.class));
        }

        @Test
        @DisplayName("Deve retornar erro 400 quando email do admin está vazio")
        void deveRetornarErroQuandoEmailAdminVazio() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("", "senha123");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve retornar erro 400 quando senha do admin está vazia")
        void deveRetornarErroQuandoSenhaAdminVazia() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("admin@example.com", "");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando corpo da requisição de admin está vazio")
        void deveRetornarErroQuandoCorpoAdminVazio() throws Exception {
            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Campos")
    class ValidacaoCamposTestes {

        @Test
        @DisplayName("Deve retornar erro quando email e senha estão vazios no login de cliente")
        void deveRetornarErroQuandoEmailESenhaVaziosCliente() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("", "");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_CLIENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando email e senha estão vazios no login de admin")
        void deveRetornarErroQuandoEmailESenhaVaziosAdmin() throws Exception {
            // Arrange
            LoginRequestDTO loginInvalido = new LoginRequestDTO("", "");

            // Act & Assert
            mockMvc.perform(post(ENDPOINT_LOGIN_ADMIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginInvalido)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify
            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }
}

