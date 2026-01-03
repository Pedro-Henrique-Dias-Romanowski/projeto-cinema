package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.ClienteValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService - Método cadastrarCliente")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteValidation clienteValidation;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Cliente clienteSalvo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setEmail("joao.silva@email.com");
        cliente.setSenha("senha123");
        cliente.setSaldo(100.0);

        clienteSalvo = new Cliente();
        clienteSalvo.setId(1L);
        clienteSalvo.setNome("João Silva");
        clienteSalvo.setEmail("joao.silva@email.com");
        clienteSalvo.setSenha("$2a$10$encodedPassword");
        clienteSalvo.setSaldo(100.0);
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode(cliente.getSenha())).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva@email.com", resultado.getEmail());
        assertEquals("$2a$10$encodedPassword", resultado.getSenha());
        assertEquals(100.0, resultado.getSaldo());

        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve chamar validação antes de cadastrar")
    void deveChamarValidacaoAntesDeCadastrar() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        clienteService.cadastrarCliente(cliente);

        // Assert
        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
    }

    @Test
    @DisplayName("Deve criptografar a senha antes de salvar")
    void deveCriptografarSenhaAntesDeSalvar() {
        // Arrange
        String senhaOriginal = "senha123";
        String senhaCriptografada = "$2a$10$encodedPassword";

        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode(senhaOriginal)).thenReturn(senhaCriptografada);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        clienteService.cadastrarCliente(cliente);

        // Assert
        verify(passwordEncoder, times(1)).encode(senhaOriginal);
        assertEquals(senhaCriptografada, cliente.getSenha());
    }


    @Test
    @DisplayName("Deve lançar EmailExistenteException quando email já existe")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // Arrange
        doThrow(new EmailExistenteException("Email já cadastrado"))
                .when(clienteValidation).validarCadastroCliente(cliente);

        // Act & Assert
        EmailExistenteException exception = assertThrows(
                EmailExistenteException.class,
                () -> clienteService.cadastrarCliente(cliente)
        );

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
        verify(passwordEncoder, never()).encode(anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar SenhaInvalidaExcpetion quando senha é inválida")
    void deveLancarExcecaoQuandoSenhaInvalida() {
        // Arrange
        doThrow(new SenhaInvalidaExcpetion("Senha deve ter entre 6 e 15 caracteres"))
                .when(clienteValidation).validarCadastroCliente(cliente);

        // Act & Assert
        SenhaInvalidaExcpetion exception = assertThrows(
                SenhaInvalidaExcpetion.class,
                () -> clienteService.cadastrarCliente(cliente)
        );

        assertEquals("Senha deve ter entre 6 e 15 caracteres", exception.getMessage());
        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
        verify(passwordEncoder, never()).encode(anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve cadastrar cliente com saldo zero")
    void deveCadastrarClienteComSaldoZero() {
        // Arrange
        Cliente clienteComSaldoZero = new Cliente();
        clienteComSaldoZero.setNome("Maria Santos");
        clienteComSaldoZero.setEmail("maria.santos@email.com");
        clienteComSaldoZero.setSenha("senha456");
        clienteComSaldoZero.setSaldo(0.0);

        Cliente clienteSalvoComSaldoZero = new Cliente();
        clienteSalvoComSaldoZero.setId(2L);
        clienteSalvoComSaldoZero.setNome("Maria Santos");
        clienteSalvoComSaldoZero.setEmail("maria.santos@email.com");
        clienteSalvoComSaldoZero.setSenha("$2a$10$encodedPassword");
        clienteSalvoComSaldoZero.setSaldo(0.0);

        doNothing().when(clienteValidation).validarCadastroCliente(clienteComSaldoZero);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvoComSaldoZero);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(clienteComSaldoZero);

        // Assert
        assertNotNull(resultado);
        assertEquals(0.0, resultado.getSaldo());
    }

    @Test
    @DisplayName("Deve cadastrar cliente com saldo máximo (1000.0)")
    void deveCadastrarClienteComSaldoMaximo() {
        // Arrange
        Cliente clienteComSaldoMaximo = new Cliente();
        clienteComSaldoMaximo.setNome("Carlos Oliveira");
        clienteComSaldoMaximo.setEmail("carlos.oliveira@email.com");
        clienteComSaldoMaximo.setSenha("senha789");
        clienteComSaldoMaximo.setSaldo(1000.0);

        Cliente clienteSalvoComSaldoMaximo = new Cliente();
        clienteSalvoComSaldoMaximo.setId(3L);
        clienteSalvoComSaldoMaximo.setNome("Carlos Oliveira");
        clienteSalvoComSaldoMaximo.setEmail("carlos.oliveira@email.com");
        clienteSalvoComSaldoMaximo.setSenha("$2a$10$encodedPassword");
        clienteSalvoComSaldoMaximo.setSaldo(1000.0);

        doNothing().when(clienteValidation).validarCadastroCliente(clienteComSaldoMaximo);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvoComSaldoMaximo);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(clienteComSaldoMaximo);

        // Assert
        assertNotNull(resultado);
        assertEquals(1000.0, resultado.getSaldo());
    }

    @Test
    @DisplayName("Deve retornar cliente com ID gerado após salvar")
    void deveRetornarClienteComIdGerado() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Deve cadastrar cliente com senha de 6 caracteres (mínimo válido)")
    void deveCadastrarClienteComSenhaMinima() {
        // Arrange
        cliente.setSenha("abc123");

        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode("abc123")).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado);
        verify(passwordEncoder, times(1)).encode("abc123");
    }

    @Test
    @DisplayName("Deve cadastrar cliente com senha de 15 caracteres (máximo válido)")
    void deveCadastrarClienteComSenhaMaxima() {
        // Arrange
        cliente.setSenha("senha123456789A");

        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(passwordEncoder.encode("senha123456789A")).thenReturn("$2a$10$encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado);
        verify(passwordEncoder, times(1)).encode("senha123456789A");
    }
}

