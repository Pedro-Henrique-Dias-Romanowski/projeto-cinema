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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void deveListarTodosClientesComSucesso() {
        // Arrange
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao.silva@email.com");
        cliente1.setSenha("$2a$10$encodedPassword");
        cliente1.setSaldo(100.0);

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNome("Maria Santos");
        cliente2.setEmail("maria.santos@email.com");
        cliente2.setSenha("$2a$10$encodedPassword2");
        cliente2.setSaldo(200.0);

        Cliente cliente3 = new Cliente();
        cliente3.setId(3L);
        cliente3.setNome("Carlos Oliveira");
        cliente3.setEmail("carlos.oliveira@email.com");
        cliente3.setSenha("$2a$10$encodedPassword3");
        cliente3.setSaldo(300.0);

        List<Cliente> clientes = List.of(cliente1, cliente2, cliente3);

        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.listarClientes();

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Santos", resultado.get(1).getNome());
        assertEquals("Carlos Oliveira", resultado.get(2).getNome());
        assertEquals(100.0, resultado.get(0).getSaldo());
        assertEquals(200.0, resultado.get(1).getSaldo());
        assertEquals(300.0, resultado.get(2).getSaldo());

        verify(clienteValidation, times(1)).validarListagemClientes();
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve chamar validação antes de listar clientes")
    void deveChamarValidacaoAntesDeListar() {
        // Arrange
        List<Cliente> clientes = new ArrayList<>();
        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        clienteService.listarClientes();

        // Assert
        verify(clienteValidation, times(1)).validarListagemClientes();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        // Arrange
        List<Cliente> clientesVazio = new ArrayList<>();
        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientesVazio);

        // Act
        List<Cliente> resultado = clienteService.listarClientes();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.size());

        verify(clienteValidation, times(1)).validarListagemClientes();
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar apenas um cliente quando há apenas um cadastrado")
    void deveListarApenasUmCliente() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao.silva@email.com");
        cliente.setSenha("$2a$10$encodedPassword");
        cliente.setSaldo(100.0);

        List<Cliente> clientes = List.of(cliente);

        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.listarClientes();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("joao.silva@email.com", resultado.get(0).getEmail());
        assertEquals(100.0, resultado.get(0).getSaldo());

        verify(clienteValidation, times(1)).validarListagemClientes();
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar clientes com saldos diferentes")
    void deveListarClientesComSaldosDiferentes() {
        // Arrange
        Cliente clienteSaldoZero = new Cliente();
        clienteSaldoZero.setId(1L);
        clienteSaldoZero.setNome("Cliente Saldo Zero");
        clienteSaldoZero.setEmail("zero@email.com");
        clienteSaldoZero.setSaldo(0.0);

        Cliente clienteSaldoMaximo = new Cliente();
        clienteSaldoMaximo.setId(2L);
        clienteSaldoMaximo.setNome("Cliente Saldo Máximo");
        clienteSaldoMaximo.setEmail("maximo@email.com");
        clienteSaldoMaximo.setSaldo(1000.0);

        List<Cliente> clientes = List.of(clienteSaldoZero, clienteSaldoMaximo);

        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.listarClientes();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(0.0, resultado.get(0).getSaldo());
        assertEquals(1000.0, resultado.get(1).getSaldo());

        verify(clienteValidation, times(1)).validarListagemClientes();
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista não nula mesmo quando vazia")
    void deveRetornarListaNaoNulaMesmoQuandoVazia() {
        // Arrange
        List<Cliente> clientesVazio = new ArrayList<>();
        doNothing().when(clienteValidation).validarListagemClientes();
        when(clienteRepository.findAll()).thenReturn(clientesVazio);

        // Act
        List<Cliente> resultado = clienteService.listarClientes();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() {
        // Arrange
        Long clienteId = 1L;
        doNothing().when(clienteValidation).validarBuscaPorCliente(clienteId);
        doNothing().when(clienteRepository).deleteById(clienteId);

        // Act & Assert
        assertDoesNotThrow(() -> clienteService.deletarCliente(clienteId));

        verify(clienteValidation, times(1)).validarBuscaPorCliente(clienteId);
        verify(clienteRepository, times(1)).deleteById(clienteId);
    }
}

