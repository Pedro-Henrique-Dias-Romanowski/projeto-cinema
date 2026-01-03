package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteValidation - Método validarCadastroCliente")
class ClienteValidationTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteValidation clienteValidation;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteValidation, "mensagemEmailExistente", "Email já cadastrado no sistema");
        ReflectionTestUtils.setField(clienteValidation, "mensagemClienteNulo", "Cliente não pode ser nulo");
        ReflectionTestUtils.setField(clienteValidation, "mensagemSenhaInvalida", "Senha deve ter entre 6 e 15 caracteres");

        cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setEmail("joao.silva@email.com");
        cliente.setSenha("senha123");
        cliente.setSaldo(100.0);
    }

    @Test
    @DisplayName("Deve validar cliente com sucesso quando todos os dados são válidos")
    void deveValidarClienteComSucesso() {
        // Arrange
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> clienteValidation.validarCadastroCliente(cliente));
        verify(clienteRepository, times(1)).existsByEmail(cliente.getEmail());
    }

    @Test
    @DisplayName("Deve lançar EmailExistenteException quando email já existe")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // Arrange
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(true);

        // Act & Assert
        EmailExistenteException exception = assertThrows(
                EmailExistenteException.class,
                () -> clienteValidation.validarCadastroCliente(cliente)
        );

        assertEquals("Email já cadastrado no sistema", exception.getMessage());
        verify(clienteRepository, times(1)).existsByEmail(cliente.getEmail());
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaExcpetion quando senha tem menos de 6 caracteres")
    void deveLancarExcecaoQuandoSenhaMenorQue6Caracteres() {
        // Arrange
        cliente.setSenha("12345"); // 5 caracteres
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        SenhaInvalidaExcpetion exception = assertThrows(
                SenhaInvalidaExcpetion.class,
                () -> clienteValidation.validarCadastroCliente(cliente)
        );

        assertEquals("Senha deve ter entre 6 e 15 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaExcpetion quando senha tem exatamente 5 caracteres")
    void deveLancarExcecaoQuandoSenhaTem5Caracteres() {
        // Arrange
        cliente.setSenha("abc12"); // Exatamente 5 caracteres (limite inválido)
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        SenhaInvalidaExcpetion exception = assertThrows(
                SenhaInvalidaExcpetion.class,
                () -> clienteValidation.validarCadastroCliente(cliente)
        );

        assertEquals("Senha deve ter entre 6 e 15 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar senha com exatamente 6 caracteres (mínimo válido)")
    void deveValidarSenhaComSeisCaracteres() {
        // Arrange
        cliente.setSenha("abc123"); // Exatamente 6 caracteres (mínimo válido)
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> clienteValidation.validarCadastroCliente(cliente));
    }

    @Test
    @DisplayName("Deve validar senha com exatamente 15 caracteres (máximo válido)")
    void deveValidarSenhaComQuinzeCaracteres() {
        // Arrange
        cliente.setSenha("senha123456789A"); // Exatamente 15 caracteres (máximo válido)
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> clienteValidation.validarCadastroCliente(cliente));
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaExcpetion quando senha tem mais de 15 caracteres")
    void deveLancarExcecaoQuandoSenhaMaiorQue15Caracteres() {
        // Arrange
        cliente.setSenha("senha1234567890ABC"); // 18 caracteres
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        SenhaInvalidaExcpetion exception = assertThrows(
                SenhaInvalidaExcpetion.class,
                () -> clienteValidation.validarCadastroCliente(cliente)
        );

        assertEquals("Senha deve ter entre 6 e 15 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaExcpetion quando senha está vazia")
    void deveLancarExcecaoQuandoSenhaVazia() {
        // Arrange
        cliente.setSenha(""); // Senha vazia
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        SenhaInvalidaExcpetion exception = assertThrows(
                SenhaInvalidaExcpetion.class,
                () -> clienteValidation.validarCadastroCliente(cliente)
        );

        assertEquals("Senha deve ter entre 6 e 15 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Deve chamar repositório apenas uma vez para verificar email")
    void deveChamarRepositorioApenasUmaVez() {
        // Arrange
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act
        clienteValidation.validarCadastroCliente(cliente);

        // Assert
        verify(clienteRepository, times(1)).existsByEmail(cliente.getEmail());
    }

    @Test
    @DisplayName("Deve validar cliente com email único e senha válida")
    void deveValidarClienteComEmailUnicoESenhaValida() {
        // Arrange
        cliente.setEmail("email.novo@email.com");
        cliente.setSenha("senha123");
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> clienteValidation.validarCadastroCliente(cliente));
        verify(clienteRepository, times(1)).existsByEmail("email.novo@email.com");
    }


    @Test
    @DisplayName("Deve validar diferentes formatos de email quando único")
    void deveValidarDiferentesFormatosDeEmail() {
        // Arrange
        cliente.setEmail("usuario.teste+tag@dominio.com.br");
        when(clienteRepository.existsByEmail(cliente.getEmail())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> clienteValidation.validarCadastroCliente(cliente));
    }
}

