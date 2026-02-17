package com.romanowski.pedro.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.romanowski.pedro.entity.AdministradorEntity;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.repository.AdministradorRepository;
import com.romanowski.pedro.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para LoginService")
class LoginServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private LoginService loginService;

    private ClienteEntity clienteEntity;
    private AdministradorEntity administradorEntity;
    private UUID clienteId;
    private UUID adminId;
    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-generation";

    @BeforeEach
    void setUp() {
        // Configura o JWT_SECRET usando reflection
        ReflectionTestUtils.setField(loginService, "JWT_SECRET", JWT_SECRET);

        // Gera IDs únicos para cada teste
        clienteId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(clienteId);
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("joao@example.com");
        clienteEntity.setSenha("senhaEncriptada");
        clienteEntity.setSaldo(500.0);
        clienteEntity.setPerfil(Perfil.CLIENTE);

        administradorEntity = new AdministradorEntity();
        administradorEntity.setId(adminId);
        administradorEntity.setNome("Admin Silva");
        administradorEntity.setEmail("admin@example.com");
        administradorEntity.setSenha("senhaEncriptada");
        administradorEntity.setPerfil(Perfil.ADMIN);
    }

    @Nested
    @DisplayName("Testes de loadUserByUsername")
    class LoadUserByUsernameTestes {

        @Test
        @DisplayName("Deve carregar cliente por email com sucesso")
        void deveCarregarClientePorEmailComSucesso() {
            // Arrange
            String email = "joao@example.com";
            when(clienteRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(clienteEntity));

            // Act
            UserDetails resultado = loginService.loadUserByUsername(email);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo(email);
            assertThat(resultado).isInstanceOf(ClienteEntity.class);
            assertThat(((ClienteEntity) resultado).getNome()).isEqualTo("João Silva");

            // Verify
            verify(clienteRepository, times(1)).findByEmailIgnoreCase(email);
            verify(administradorRepository, never()).findByEmailIgnoreCase(anyString());
        }

        @Test
        @DisplayName("Deve carregar administrador por email quando cliente não é encontrado")
        void deveCarregarAdministradorPorEmailQuandoClienteNaoEncontrado() {
            // Arrange
            String email = "admin@example.com";
            when(clienteRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
            when(administradorRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(administradorEntity));

            // Act
            UserDetails resultado = loginService.loadUserByUsername(email);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo(email);
            assertThat(resultado).isInstanceOf(AdministradorEntity.class);
            assertThat(((AdministradorEntity) resultado).getNome()).isEqualTo("Admin Silva");

            // Verify
            verify(clienteRepository, times(1)).findByEmailIgnoreCase(email);
            verify(administradorRepository, times(1)).findByEmailIgnoreCase(email);
        }

        @Test
        @DisplayName("Deve lançar UsernameNotFoundException quando usuário não é encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            // Arrange
            String email = "inexistente@example.com";
            when(clienteRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
            when(administradorRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> loginService.loadUserByUsername(email))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("Usuario nao encontrado");

            // Verify
            verify(clienteRepository, times(1)).findByEmailIgnoreCase(email);
            verify(administradorRepository, times(1)).findByEmailIgnoreCase(email);
        }

        @Test
        @DisplayName("Deve buscar em ambos repositórios quando cliente não existe mas admin existe")
        void deveBuscarEmAmbosRepositoriosQuandoNecessario() {
            // Arrange
            String email = "admin@example.com";
            when(clienteRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
            when(administradorRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(administradorEntity));

            // Act
            loginService.loadUserByUsername(email);

            // Assert & Verify
            verify(clienteRepository).findByEmailIgnoreCase(email);
            verify(administradorRepository).findByEmailIgnoreCase(email);
        }
    }

    @Nested
    @DisplayName("Testes de gerarTokenCliente")
    class GerarTokenClienteTestes {

        @Test
        @DisplayName("Deve gerar token JWT válido para cliente")
        void deveGerarTokenJWTValidoParaCliente() {
            // Act
            String token = loginService.gerarTokenCliente(clienteEntity);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes: header.payload.signature
        }

        @Test
        @DisplayName("Deve incluir informações corretas no token do cliente")
        void deveIncluirInformacoesCorretasNoTokenCliente() {
            // Act
            String token = loginService.gerarTokenCliente(clienteEntity);

            // Assert - Decodifica o token para verificar claims
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getSubject()).isEqualTo(clienteId.toString());
            assertThat(decodedJWT.getClaim("email").asString()).isEqualTo("joao@example.com");
            assertThat(decodedJWT.getClaim("roles").asList(String.class)).containsExactly("CLIENTE");
            assertThat(decodedJWT.getIssuer()).isEqualTo("cinecom-auth");
        }

        @Test
        @DisplayName("Deve incluir data de emissão no token do cliente")
        void deveIncluirDataEmissaoNoTokenCliente() {
            // Act
            String token = loginService.gerarTokenCliente(clienteEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getIssuedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve incluir data de expiração no token do cliente")
        void deveIncluirDataExpiracaoNoTokenCliente() {
            // Act
            String token = loginService.gerarTokenCliente(clienteEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getExpiresAt()).isNotNull();
            assertThat(decodedJWT.getExpiresAt()).isAfter(decodedJWT.getIssuedAt());
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para clientes diferentes")
        void deveGerarTokensDiferentesParaClientesDiferentes() {
            // Arrange
            ClienteEntity outroCliente = new ClienteEntity();
            outroCliente.setId(UUID.randomUUID());
            outroCliente.setNome("Maria Santos");
            outroCliente.setEmail("maria@example.com");
            outroCliente.setSenha("senhaEncriptada");
            outroCliente.setSaldo(300.0);
            outroCliente.setPerfil(Perfil.CLIENTE);

            // Act
            String token1 = loginService.gerarTokenCliente(clienteEntity);
            String token2 = loginService.gerarTokenCliente(outroCliente);

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Testes de gerarTokenAdministrador")
    class GerarTokenAdministradorTestes {

        @Test
        @DisplayName("Deve gerar token JWT válido para administrador")
        void deveGerarTokenJWTValidoParaAdministrador() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes: header.payload.signature
        }

        @Test
        @DisplayName("Deve incluir informações corretas no token do administrador")
        void deveIncluirInformacoesCorretasNoTokenAdministrador() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert - Decodifica o token para verificar claims
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getSubject()).isEqualTo(adminId.toString());
            assertThat(decodedJWT.getClaim("email").asString()).isEqualTo("admin@example.com");
            assertThat(decodedJWT.getClaim("roles").asList(String.class)).containsExactly("ADMIN");
            assertThat(decodedJWT.getIssuer()).isEqualTo("cinecom-auth");
        }

        @Test
        @DisplayName("Deve incluir role ADMIN no token do administrador")
        void deveIncluirRoleAdminNoToken() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getClaim("roles").asList(String.class))
                    .containsExactly("ADMIN")
                    .doesNotContain("CLIENTE");
        }

        @Test
        @DisplayName("Deve incluir data de emissão no token do administrador")
        void deveIncluirDataEmissaoNoTokenAdministrador() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getIssuedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve incluir data de expiração no token do administrador")
        void deveIncluirDataExpiracaoNoTokenAdministrador() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getExpiresAt()).isNotNull();
            assertThat(decodedJWT.getExpiresAt()).isAfter(decodedJWT.getIssuedAt());
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para administradores diferentes")
        void deveGerarTokensDiferentesParaAdministradoresDiferentes() {
            // Arrange
            AdministradorEntity outroAdmin = new AdministradorEntity();
            outroAdmin.setId(UUID.randomUUID());
            outroAdmin.setNome("Outro Admin");
            outroAdmin.setEmail("outro@example.com");
            outroAdmin.setSenha("senhaEncriptada");
            outroAdmin.setPerfil(Perfil.ADMIN);

            // Act
            String token1 = loginService.gerarTokenAdministrador(administradorEntity);
            String token2 = loginService.gerarTokenAdministrador(outroAdmin);

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Testes de Diferenças entre Tokens")
    class DiferencasTokensTestes {

        @Test
        @DisplayName("Token de cliente deve ter role CLIENTE e não ADMIN")
        void tokenClienteDeveTerRoleCliente() {
            // Act
            String token = loginService.gerarTokenCliente(clienteEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getClaim("roles").asList(String.class))
                    .containsExactly("CLIENTE")
                    .doesNotContain("ADMIN");
        }

        @Test
        @DisplayName("Token de administrador deve ter role ADMIN e não CLIENTE")
        void tokenAdministradorDeveTerRoleAdmin() {
            // Act
            String token = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getClaim("roles").asList(String.class))
                    .containsExactly("ADMIN")
                    .doesNotContain("CLIENTE");
        }

        @Test
        @DisplayName("Token de cliente e administrador devem ter mesmo issuer")
        void tokensDevemTerMesmoIssuer() {
            // Act
            String tokenCliente = loginService.gerarTokenCliente(clienteEntity);
            String tokenAdmin = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

            DecodedJWT decodedCliente = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(tokenCliente);

            DecodedJWT decodedAdmin = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(tokenAdmin);

            assertThat(decodedCliente.getIssuer()).isEqualTo(decodedAdmin.getIssuer());
            assertThat(decodedCliente.getIssuer()).isEqualTo("cinecom-auth");
        }

        @Test
        @DisplayName("Tokens devem ter subjects diferentes baseados em IDs diferentes")
        void tokensDevemTerSubjectsDiferentes() {
            // Act
            String tokenCliente = loginService.gerarTokenCliente(clienteEntity);
            String tokenAdmin = loginService.gerarTokenAdministrador(administradorEntity);

            // Assert
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

            DecodedJWT decodedCliente = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(tokenCliente);

            DecodedJWT decodedAdmin = JWT.require(algorithm)
                    .withIssuer("cinecom-auth")
                    .build()
                    .verify(tokenAdmin);

            assertThat(decodedCliente.getSubject()).isEqualTo(clienteId.toString());
            assertThat(decodedAdmin.getSubject()).isEqualTo(adminId.toString());
            assertThat(decodedCliente.getSubject()).isNotEqualTo(decodedAdmin.getSubject());
        }
    }
}

