package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaException;
import com.romanowski.pedro.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CadastroClienteValidation")
class CadastroClienteValidationTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CadastroClienteValidation cadastroClienteValidation;

    private ClienteEntity clienteEntity;

    @BeforeEach
    void setUp() {
        // Configurar as mensagens via reflection já que são injetadas via @Value
        ReflectionTestUtils.setField(cadastroClienteValidation, "mensagemEmailExistente", "Email já cadastrado no sistema");
        ReflectionTestUtils.setField(cadastroClienteValidation, "mensagemSenhaInvalida", "Senha deve ter entre 6 e 15 caracteres");

        clienteEntity = new ClienteEntity();
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("joao@example.com");
        clienteEntity.setSenha("senha123");
        clienteEntity.setSaldo(500.0);
    }

    @Nested
    @DisplayName("Validação de Email")
    class ValidacaoEmail {

        @Test
        @DisplayName("Deve aceitar email não cadastrado")
        void deveAceitarEmailNaoCadastrado() {
            // Arrange
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatCode(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .doesNotThrowAnyException();

            verify(clienteRepository).existsByEmail(clienteEntity.getEmail());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            when(clienteRepository.existsByEmail(clienteEntity.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .isInstanceOf(EmailExistenteException.class)
                    .hasMessage("Email já cadastrado no sistema");

            verify(clienteRepository).existsByEmail(clienteEntity.getEmail());
        }

    }

    @Nested
    @DisplayName("Validação de Senha")
    class ValidacaoSenha {

        @ParameterizedTest(name = "Deve aceitar senha válida: \"{0}\"")
        @ValueSource(strings = {"senha1", "123456", "senhaValida123", "quinzeCaracter"})
        @DisplayName("Deve aceitar senhas com tamanho entre 6 e 15 caracteres")
        void deveAceitarSenhaComTamanhoValido(String senhaValida) {
            // Arrange
            clienteEntity.setSenha(senhaValida);
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatCode(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "Deve rejeitar senha muito curta: \"{0}\"")
        @ValueSource(strings = {"12345", "abc", "a", ""})
        @DisplayName("Deve lançar exceção para senha com 5 ou menos caracteres")
        void deveLancarExcecaoParaSenhaMuitoCurta(String senhaCurta) {
            // Arrange
            clienteEntity.setSenha(senhaCurta);
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .isInstanceOf(SenhaInvalidaException.class)
                    .hasMessage("Senha deve ter entre 6 e 15 caracteres");
        }

        @ParameterizedTest(name = "Deve rejeitar senha muito longa: \"{0}\"")
        @ValueSource(strings = {"1234567890123456", "senhaComMuitosCaracteres", "estaSenhaEMuitoLongaParaSerValida"})
        @DisplayName("Deve lançar exceção para senha com mais de 15 caracteres")
        void deveLancarExcecaoParaSenhaMuitoLonga(String senhaLonga) {
            // Arrange
            clienteEntity.setSenha(senhaLonga);
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .isInstanceOf(SenhaInvalidaException.class)
                    .hasMessage("Senha deve ter entre 6 e 15 caracteres");
        }

        @Test
        @DisplayName("Deve aceitar senha exatamente com 6 caracteres (limite mínimo)")
        void deveAceitarSenhaComExatamente6Caracteres() {
            // Arrange
            clienteEntity.setSenha("123456");
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatCode(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar senha exatamente com 15 caracteres (limite máximo)")
        void deveAceitarSenhaComExatamente15Caracteres() {
            // Arrange
            clienteEntity.setSenha("123456789012345");
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            assertThatCode(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Cenários de Sucesso Completo")
    class CenariosSucessoCompleto {

        @Test
        @DisplayName("Deve passar validação quando email não existe e senha é válida")
        void devePassarValidacaoComDadosValidos() {
            // Arrange
            clienteEntity.setEmail("novo@example.com");
            clienteEntity.setSenha("senhaValida");
            when(clienteRepository.existsByEmail("novo@example.com")).thenReturn(false);

            // Act & Assert
            assertThatCode(() -> cadastroClienteValidation.validarCadastroCliente(clienteEntity))
                    .doesNotThrowAnyException();

            verify(clienteRepository).existsByEmail("novo@example.com");
        }
    }
}

