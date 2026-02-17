package com.romanowski.pedro.controller;

import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private ClienteRequestDTO clienteRequestDTO;
    private Cliente clienteEntity;
    private Cliente clienteSalvo;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        UUID clienteUuid = UUID.randomUUID();

        clienteRequestDTO = new ClienteRequestDTO(
                clienteUuid,
                "João Silva",
                "joao.silva@email.com",
                "senha123",
                100.0
        );

        clienteEntity = new Cliente();
        clienteEntity.setId(clienteUuid);
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("joao.silva@email.com");
        clienteEntity.setSenha("senha123");
        clienteEntity.setSaldo(100.0);

        clienteSalvo = new Cliente();
        clienteSalvo.setId(clienteUuid);
        clienteSalvo.setNome("João Silva");
        clienteSalvo.setEmail("joao.silva@email.com");
        clienteSalvo.setSenha("$2a$10$encodedPassword");
        clienteSalvo.setSaldo(100.0);

        clienteResponseDTO = new ClienteResponseDTO(
                clienteUuid,
                "João Silva",
                "joao.silva@email.com",
                100.0
        );
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso e retornar status 200")
    void deveCadastrarClienteComSucesso() {
        // Arrange
        when(clienteMapper.toEntity(clienteRequestDTO)).thenReturn(clienteEntity);
        when(clienteService.cadastrarCliente(clienteEntity)).thenReturn(clienteSalvo);
        when(clienteMapper.toResponseDTO(clienteSalvo)).thenReturn(clienteResponseDTO);

        // Act
        ResponseEntity<ClienteResponseDTO> response = clienteController.cadastrarCliente(clienteRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals("João Silva", response.getBody().nome());
        assertEquals("joao.silva@email.com", response.getBody().email());
        assertEquals(100.0, response.getBody().saldo());

        verify(clienteMapper, times(1)).toEntity(clienteRequestDTO);
        verify(clienteService, times(1)).cadastrarCliente(clienteEntity);
        verify(clienteMapper, times(1)).toResponseDTO(clienteSalvo);
    }

    @Test
    @DisplayName("Deve cadastrar cliente com saldo zero")
    void deveCadastrarClienteComSaldoZero() {
        // Arrange
        UUID clienteUuid = UUID.randomUUID();

        ClienteRequestDTO requestComSaldoZero = new ClienteRequestDTO(
                clienteUuid,
                "Maria Santos",
                "maria.santos@email.com",
                "senha456",
                0.0
        );

        Cliente entityComSaldoZero = new Cliente();
        entityComSaldoZero.setId(clienteUuid);
        entityComSaldoZero.setNome("Maria Santos");
        entityComSaldoZero.setEmail("maria.santos@email.com");
        entityComSaldoZero.setSenha("senha456");
        entityComSaldoZero.setSaldo(0.0);

        Cliente salvoComSaldoZero = new Cliente();
        salvoComSaldoZero.setId(clienteUuid);
        salvoComSaldoZero.setNome("Maria Santos");
        salvoComSaldoZero.setEmail("maria.santos@email.com");
        salvoComSaldoZero.setSenha("$2a$10$encodedPassword");
        salvoComSaldoZero.setSaldo(0.0);

        ClienteResponseDTO responseComSaldoZero = new ClienteResponseDTO(
                clienteUuid,
                "Maria Santos",
                "maria.santos@email.com",
                0.0
        );

        when(clienteMapper.toEntity(requestComSaldoZero)).thenReturn(entityComSaldoZero);
        when(clienteService.cadastrarCliente(entityComSaldoZero)).thenReturn(salvoComSaldoZero);
        when(clienteMapper.toResponseDTO(salvoComSaldoZero)).thenReturn(responseComSaldoZero);

        // Act
        ResponseEntity<ClienteResponseDTO> response = clienteController.cadastrarCliente(requestComSaldoZero);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().saldo());
    }

    @Test
    @DisplayName("Deve cadastrar cliente com saldo máximo permitido (1000.0)")
    void deveCadastrarClienteComSaldoMaximo() {
        // Arrange
        UUID clienteUuid = UUID.randomUUID();

        ClienteRequestDTO requestComSaldoMaximo = new ClienteRequestDTO(
                clienteUuid,
                "Carlos Oliveira",
                "carlos.oliveira@email.com",
                "senha789",
                1000.0
        );

        Cliente entityComSaldoMaximo = new Cliente();
        entityComSaldoMaximo.setId(clienteUuid);
        entityComSaldoMaximo.setNome("Carlos Oliveira");
        entityComSaldoMaximo.setEmail("carlos.oliveira@email.com");
        entityComSaldoMaximo.setSenha("senha789");
        entityComSaldoMaximo.setSaldo(1000.0);

        Cliente salvoComSaldoMaximo = new Cliente();
        salvoComSaldoMaximo.setId(clienteUuid);
        salvoComSaldoMaximo.setNome("Carlos Oliveira");
        salvoComSaldoMaximo.setEmail("carlos.oliveira@email.com");
        salvoComSaldoMaximo.setSenha("$2a$10$encodedPassword");
        salvoComSaldoMaximo.setSaldo(1000.0);

        ClienteResponseDTO responseComSaldoMaximo = new ClienteResponseDTO(
                clienteUuid,
                "Carlos Oliveira",
                "carlos.oliveira@email.com",
                1000.0
        );

        when(clienteMapper.toEntity(requestComSaldoMaximo)).thenReturn(entityComSaldoMaximo);
        when(clienteService.cadastrarCliente(entityComSaldoMaximo)).thenReturn(salvoComSaldoMaximo);
        when(clienteMapper.toResponseDTO(salvoComSaldoMaximo)).thenReturn(responseComSaldoMaximo);

        // Act
        ResponseEntity<ClienteResponseDTO> response = clienteController.cadastrarCliente(requestComSaldoMaximo);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000.0, response.getBody().saldo());
    }

    @Test
    @DisplayName("Deve retornar ResponseEntity com corpo não nulo")
    void deveRetornarResponseEntityComCorpoNaoNulo() {
        // Arrange
        when(clienteMapper.toEntity(clienteRequestDTO)).thenReturn(clienteEntity);
        when(clienteService.cadastrarCliente(clienteEntity)).thenReturn(clienteSalvo);
        when(clienteMapper.toResponseDTO(clienteSalvo)).thenReturn(clienteResponseDTO);

        // Act
        ResponseEntity<ClienteResponseDTO> response = clienteController.cadastrarCliente(clienteRequestDTO);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso e retornar status 200")
    void deveListarTodosClientesComSucesso() {
        // Arrange
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        Cliente cliente1 = new Cliente();
        cliente1.setId(uuid1);
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao.silva@email.com");
        cliente1.setSenha("$2a$10$encodedPassword");
        cliente1.setSaldo(100.0);

        Cliente cliente2 = new Cliente();
        cliente2.setId(uuid2);
        cliente2.setNome("Maria Santos");
        cliente2.setEmail("maria.santos@email.com");
        cliente2.setSenha("$2a$10$encodedPassword2");
        cliente2.setSaldo(200.0);

        Cliente cliente3 = new Cliente();
        cliente3.setId(uuid3);
        cliente3.setNome("Carlos Oliveira");
        cliente3.setEmail("carlos.oliveira@email.com");
        cliente3.setSenha("$2a$10$encodedPassword3");
        cliente3.setSaldo(300.0);

        List<Cliente> clientes = List.of(cliente1, cliente2, cliente3);

        ClienteResponseDTO response1 = new ClienteResponseDTO(uuid1, "João Silva", "joao.silva@email.com", 100.0);
        ClienteResponseDTO response2 = new ClienteResponseDTO(uuid2, "Maria Santos", "maria.santos@email.com", 200.0);
        ClienteResponseDTO response3 = new ClienteResponseDTO(uuid3, "Carlos Oliveira", "carlos.oliveira@email.com", 300.0);

        when(clienteService.listarClientes()).thenReturn(clientes);
        when(clienteMapper.toResponseDTO(cliente1)).thenReturn(response1);
        when(clienteMapper.toResponseDTO(cliente2)).thenReturn(response2);
        when(clienteMapper.toResponseDTO(cliente3)).thenReturn(response3);

        // Act
        ResponseEntity<List<ClienteResponseDTO>> response = clienteController.listarClientes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("João Silva", response.getBody().get(0).nome());
        assertEquals("Maria Santos", response.getBody().get(1).nome());
        assertEquals("Carlos Oliveira", response.getBody().get(2).nome());

        verify(clienteService, times(1)).listarClientes();
        verify(clienteMapper, times(1)).toResponseDTO(cliente1);
        verify(clienteMapper, times(1)).toResponseDTO(cliente2);
        verify(clienteMapper, times(1)).toResponseDTO(cliente3);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        // Arrange
        List<Cliente> clientesVazio = new ArrayList<>();
        when(clienteService.listarClientes()).thenReturn(clientesVazio);

        // Act
        ResponseEntity<List<ClienteResponseDTO>> response = clienteController.listarClientes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals(0, response.getBody().size());

        verify(clienteService, times(1)).listarClientes();
        verify(clienteMapper, never()).toResponseDTO(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve listar apenas um cliente quando há apenas um cadastrado")
    void deveListarApenasUmCliente() {
        // Arrange
        UUID clienteUuid = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setId(clienteUuid);
        cliente.setNome("João Silva");
        cliente.setEmail("joao.silva@email.com");
        cliente.setSenha("$2a$10$encodedPassword");
        cliente.setSaldo(100.0);

        List<Cliente> clientes = List.of(cliente);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(clienteUuid, "João Silva", "joao.silva@email.com", 100.0);

        when(clienteService.listarClientes()).thenReturn(clientes);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        // Act
        ResponseEntity<List<ClienteResponseDTO>> response = clienteController.listarClientes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("João Silva", response.getBody().get(0).nome());

        verify(clienteService, times(1)).listarClientes();
        verify(clienteMapper, times(1)).toResponseDTO(cliente);
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso e retornar status 204")
    void deveDeletarClienteComSucesso() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        doNothing().when(clienteService).deletarCliente(clienteId);

        // Act
        ResponseEntity<Void> response = clienteController.deletarCliente(clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(clienteService, times(1)).deletarCliente(clienteId);
    }

    @Test
    @DisplayName("Deve retornar ResponseEntity com corpo nulo ao deletar")
    void deveRetornarResponseEntityComCorpoNuloAoDeletar() {
        // Arrange
        UUID clienteId = UUID.randomUUID();
        doNothing().when(clienteService).deletarCliente(clienteId);

        // Act
        ResponseEntity<Void> response = clienteController.deletarCliente(clienteId);

        // Assert
        assertNotNull(response);
        assertNull(response.getBody());
    }
}

