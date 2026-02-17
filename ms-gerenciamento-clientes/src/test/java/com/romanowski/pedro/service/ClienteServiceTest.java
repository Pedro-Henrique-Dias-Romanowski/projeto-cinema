package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.email.EmailService;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
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

    @Mock
    private EmailService emailService;

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
        clienteSalvo.setId(UUID.randomUUID());
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
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao.silva@email.com", resultado.getEmail());
        assertEquals("$2a$10$encodedPassword", resultado.getSenha());
        assertEquals(100.0, resultado.getSaldo());

        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(emailService, times(1)).enviarEmail(eq("joao.silva@email.com"), eq("Bem-vindo ao Cinema"), anyString());
    }

    @Test
    @DisplayName("Deve chamar validação antes de cadastrar")
    void deveChamarValidacaoAntesDeCadastrar() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        clienteService.cadastrarCliente(cliente);

        // Assert
        verify(clienteValidation, times(1)).validarCadastroCliente(cliente);
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
        clienteSalvoComSaldoZero.setId(UUID.randomUUID());
        clienteSalvoComSaldoZero.setNome("Maria Santos");
        clienteSalvoComSaldoZero.setEmail("maria.santos@email.com");
        clienteSalvoComSaldoZero.setSenha("$2a$10$encodedPassword");
        clienteSalvoComSaldoZero.setSaldo(0.0);

        doNothing().when(clienteValidation).validarCadastroCliente(clienteComSaldoZero);
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
        clienteSalvoComSaldoMaximo.setId(UUID.randomUUID());
        clienteSalvoComSaldoMaximo.setNome("Carlos Oliveira");
        clienteSalvoComSaldoMaximo.setEmail("carlos.oliveira@email.com");
        clienteSalvoComSaldoMaximo.setSenha("$2a$10$encodedPassword");
        clienteSalvoComSaldoMaximo.setSaldo(1000.0);

        doNothing().when(clienteValidation).validarCadastroCliente(clienteComSaldoMaximo);
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
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        Cliente resultado = clienteService.cadastrarCliente(cliente);

        // Assert
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void deveListarTodosClientesComSucesso() {
        // Arrange
        Cliente cliente1 = new Cliente();
        cliente1.setId(UUID.randomUUID());
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao.silva@email.com");
        cliente1.setSenha("$2a$10$encodedPassword");
        cliente1.setSaldo(100.0);

        Cliente cliente2 = new Cliente();
        cliente2.setId(UUID.randomUUID());
        cliente2.setNome("Maria Santos");
        cliente2.setEmail("maria.santos@email.com");
        cliente2.setSenha("$2a$10$encodedPassword2");
        cliente2.setSaldo(200.0);

        Cliente cliente3 = new Cliente();
        cliente3.setId(UUID.randomUUID());
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
        cliente.setId(UUID.randomUUID());
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
        clienteSaldoZero.setId(UUID.randomUUID());
        clienteSaldoZero.setNome("Cliente Saldo Zero");
        clienteSaldoZero.setEmail("zero@email.com");
        clienteSaldoZero.setSaldo(0.0);

        Cliente clienteSaldoMaximo = new Cliente();
        clienteSaldoMaximo.setId(UUID.randomUUID());
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
    @DisplayName("Deve enviar email de boas-vindas ao cadastrar cliente")
    void deveEnviarEmailBoasVindasAoCadastrarCliente() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        clienteService.cadastrarCliente(cliente);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                eq("joao.silva@email.com"),
                eq("Bem-vindo ao Cinema"),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve enviar email com o nome correto do cliente no cadastro")
    void deveEnviarEmailComNomeCorretoNoCadastro() {
        // Arrange
        doNothing().when(clienteValidation).validarCadastroCliente(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        clienteService.cadastrarCliente(cliente);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                anyString(),
                anyString(),
                contains("João Silva")
        );
    }

    @Test
    @DisplayName("Não deve enviar email quando validação falha no cadastro")
    void naoDeveEnviarEmailQuandoValidacaoFalhaNoCadastro() {
        // Arrange
        doThrow(new EmailExistenteException("Email já cadastrado"))
                .when(clienteValidation).validarCadastroCliente(cliente);

        // Act & Assert
        assertThrows(EmailExistenteException.class, () -> clienteService.cadastrarCliente(cliente));

        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }


    @Test
    @DisplayName("Deve enviar email de despedida ao deletar cliente")
    void deveEnviarEmailDespedidaAoDeletarCliente() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        doNothing().when(clienteValidation).validarBuscaPorCliente(clienteId);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).deleteById(clienteId);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        clienteService.deletarCliente(clienteId);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                eq("joao.silva@email.com"),
                eq("Tchau, até a próxima"),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve enviar email com o nome correto do cliente na exclusão")
    void deveEnviarEmailComNomeCorretoNaExclusao() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        doNothing().when(clienteValidation).validarBuscaPorCliente(clienteId);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).deleteById(clienteId);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Act
        clienteService.deletarCliente(clienteId);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                anyString(),
                anyString(),
                contains("João Silva")
        );
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        doNothing().when(clienteValidation).validarBuscaPorCliente(clienteId);
        doNothing().when(clienteRepository).deleteById(clienteId);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertDoesNotThrow(() -> clienteService.deletarCliente(clienteId));

        verify(clienteValidation, times(1)).validarBuscaPorCliente(clienteId);
        verify(clienteRepository, times(1)).deleteById(clienteId);
        verify(emailService, times(1)).enviarEmail(eq("joao.silva@email.com"), eq("Tchau, até a próxima"), anyString());
    }
}

