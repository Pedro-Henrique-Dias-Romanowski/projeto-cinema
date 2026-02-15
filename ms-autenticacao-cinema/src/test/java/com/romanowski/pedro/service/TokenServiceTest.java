package com.romanowski.pedro.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para TokenService")
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private static final String JWT_SECRET = "test-secret-key-for-jwt-token-generation";
    private String tokenValido;
    private String tokenExpirado;
    private String tokenComIssuerInvalido;

    @BeforeEach
    void setUp() {
        // Configura o JWT_SECRET usando reflection
        ReflectionTestUtils.setField(tokenService, "JWT_SECRET", JWT_SECRET);

        // Cria um token válido
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
        tokenValido = JWT.create()
                .withIssuer("Cinecom")
                .withSubject("123")
                .withClaim("email", "usuario@example.com")
                .withClaim("roles", List.of("CLIENTE"))
                .withIssuedAt(new Date())
                .withExpiresAt(dataExpiracaoFutura())
                .sign(algorithm);

        // Cria um token expirado
        tokenExpirado = JWT.create()
                .withIssuer("Cinecom")
                .withSubject("456")
                .withClaim("email", "expirado@example.com")
                .withClaim("roles", List.of("CLIENTE"))
                .withIssuedAt(dataPassada())
                .withExpiresAt(dataExpiracaoPassada())
                .sign(algorithm);

        // Cria um token com issuer inválido
        tokenComIssuerInvalido = JWT.create()
                .withIssuer("IssuerInvalido")
                .withSubject("789")
                .withClaim("email", "invalido@example.com")
                .withIssuedAt(new Date())
                .withExpiresAt(dataExpiracaoFutura())
                .sign(algorithm);
    }

    private Date dataExpiracaoFutura() {
        Instant futuro = LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.of("-03:00"));
        return Date.from(futuro);
    }

    private Date dataExpiracaoPassada() {
        Instant passado = LocalDateTime.now().minusHours(1).toInstant(ZoneOffset.of("-03:00"));
        return Date.from(passado);
    }

    private Date dataPassada() {
        Instant passado = LocalDateTime.now().minusHours(2).toInstant(ZoneOffset.of("-03:00"));
        return Date.from(passado);
    }

    @Nested
    @DisplayName("Testes de verificarToken - Cenários de Sucesso")
    class VerificarTokenSucessoTestes {

        @Test
        @DisplayName("Deve verificar token válido com sucesso e retornar o subject")
        void deveVerificarTokenValidoComSucesso() {
            // Act
            String subject = tokenService.verificarToken(tokenValido);

            // Assert
            assertThat(subject).isNotNull();
            assertThat(subject).isEqualTo("123");
        }

        @Test
        @DisplayName("Deve retornar subject correto do token")
        void deveRetornarSubjectCorretoDoToken() {
            // Arrange
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            String tokenComSubject = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("999")
                    .withClaim("email", "teste@example.com")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            // Act
            String subject = tokenService.verificarToken(tokenComSubject);

            // Assert
            assertThat(subject).isEqualTo("999");
        }

        @Test
        @DisplayName("Deve verificar token com diferentes subjects")
        void deveVerificarTokenComDiferentesSubjects() {
            // Arrange
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

            String token1 = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("100")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            String token2 = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("200")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            // Act
            String subject1 = tokenService.verificarToken(token1);
            String subject2 = tokenService.verificarToken(token2);

            // Assert
            assertThat(subject1).isEqualTo("100");
            assertThat(subject2).isEqualTo("200");
            assertThat(subject1).isNotEqualTo(subject2);
        }

        @Test
        @DisplayName("Deve verificar token com issuer correto Cinecom")
        void deveVerificarTokenComIssuerCorreto() {
            // Arrange
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            String tokenComIssuerCorreto = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("555")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            // Act
            String subject = tokenService.verificarToken(tokenComIssuerCorreto);

            // Assert
            assertThat(subject).isNotNull();
            assertThat(subject).isEqualTo("555");
        }
    }

    @Nested
    @DisplayName("Testes de verificarToken - Cenários de Erro")
    class VerificarTokenErroTestes {

        @Test
        @DisplayName("Deve lançar RuntimeException quando token está expirado")
        void deveLancarExcecaoQuandoTokenExpirado() {
            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenExpirado))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado")
                    .hasCauseInstanceOf(JWTVerificationException.class);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando issuer é inválido")
        void deveLancarExcecaoQuandoIssuerInvalido() {
            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenComIssuerInvalido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado")
                    .hasCauseInstanceOf(JWTVerificationException.class);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token tem formato inválido")
        void deveLancarExcecaoQuandoTokenFormatoInvalido() {
            // Arrange
            String tokenInvalido = "token.invalido.formato";

            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenInvalido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token está vazio")
        void deveLancarExcecaoQuandoTokenVazio() {
            // Arrange
            String tokenVazio = "";

            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenVazio))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token é null")
        void deveLancarExcecaoQuandoTokenNull() {
            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token foi assinado com secret diferente")
        void deveLancarExcecaoQuandoTokenAssinadoComSecretDiferente() {
            // Arrange
            Algorithm algorithmDiferente = Algorithm.HMAC256("outro-secret-key");
            String tokenComSecretDiferente = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("111")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithmDiferente);

            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenComSecretDiferente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token tem apenas duas partes")
        void deveLancarExcecaoQuandoTokenIncompletoApenasDuasPartes() {
            // Arrange
            String tokenIncompleto = "header.payload";

            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenIncompleto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando token tem conteúdo aleatório")
        void deveLancarExcecaoQuandoTokenAleatorio() {
            // Arrange
            String tokenAleatorio = "abc123xyz789";

            // Act & Assert
            assertThatThrownBy(() -> tokenService.verificarToken(tokenAleatorio))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT invalido ou expirado");
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Claims")
    class ValidacaoClaimsTestes {

        @Test
        @DisplayName("Deve aceitar token com claims adicionais válidos")
        void deveAceitarTokenComClaimsAdicionais() {
            // Arrange
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            String tokenComClaims = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("777")
                    .withClaim("email", "usuario@example.com")
                    .withClaim("roles", List.of("ADMIN"))
                    .withClaim("customClaim", "customValue")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            // Act
            String subject = tokenService.verificarToken(tokenComClaims);

            // Assert
            assertThat(subject).isEqualTo("777");
        }

        @Test
        @DisplayName("Deve verificar token sem claims opcionais")
        void deveVerificarTokenSemClaimsOpcionais() {
            // Arrange
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            String tokenMinimo = JWT.create()
                    .withIssuer("Cinecom")
                    .withSubject("888")
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoFutura())
                    .sign(algorithm);

            // Act
            String subject = tokenService.verificarToken(tokenMinimo);

            // Assert
            assertThat(subject).isEqualTo("888");
        }
    }
}

