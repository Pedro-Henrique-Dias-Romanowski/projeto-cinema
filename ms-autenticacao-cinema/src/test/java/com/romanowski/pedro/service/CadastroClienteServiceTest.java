package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.request.CadastroFeignClientRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.CadastroClienteValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CadastroClienteService")
class CadastroClienteServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CadastroClienteValidation cadastroClienteValidation;

    @Mock
    private ClienteFeignClient clienteFeignClient;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private CadastroClienteService cadastroClienteService;

    @Captor
    private ArgumentCaptor<ClienteEntity> clienteEntityCaptor;

    @Captor
    private ArgumentCaptor<CadastroFeignClientRequestDTO> cadastroFeignClientRequestDTOCaptor;

    private ClienteEntity clienteEntity;
    private CadastroFeignClientRequestDTO cadastroFeignClientRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        clienteEntity = new ClienteEntity();
        clienteEntity.setNome("João Silva");
        clienteEntity.setEmail("joao@example.com");
        clienteEntity.setSenha("senha123");
        clienteEntity.setSaldo(500.0);

        cadastroFeignClientRequestDTO = new CadastroFeignClientRequestDTO(
                UUID.randomUUID(),
                "João Silva",
                "joao@example.com",
                "senhaCriptografada",
                500.0
        );

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
        @DisplayName("Deve cadastrar cliente com sucesso")
        void deveCadastrarClienteComSucesso() {
            // Arrange
            String senhaCriptografada = "senhaCriptografada123";
            ClienteEntity clienteSalvo = criarClienteSalvo(senhaCriptografada);

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(anyString())).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);
            when(clienteMapper.toDTO(any(ClienteEntity.class))).thenReturn(cadastroFeignClientRequestDTO);
            when(clienteFeignClient.cadastrarCliente(any(CadastroFeignClientRequestDTO.class))).thenReturn(clienteResponseDTO);

            // Act
            ClienteEntity resultado = cadastroClienteService.cadastrarCliente(clienteEntity);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("joao@example.com");
            assertThat(resultado.getSenha()).isEqualTo(senhaCriptografada);
            assertThat(resultado.getPerfil()).isEqualTo(Perfil.CLIENTE);
        }

        @Test
        @DisplayName("Deve criptografar a senha antes de salvar")
        void deveCriptografarSenhaAntesDeSalvar() {
            // Arrange
            String senhaOriginal = "senha123";
            String senhaCriptografada = "$2a$10$encoded_password_hash";
            clienteEntity.setSenha(senhaOriginal);

            ClienteEntity clienteSalvo = criarClienteSalvo(senhaCriptografada);

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(senhaOriginal)).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);
            when(clienteMapper.toDTO(any(ClienteEntity.class))).thenReturn(cadastroFeignClientRequestDTO);
            when(clienteFeignClient.cadastrarCliente(any(CadastroFeignClientRequestDTO.class))).thenReturn(clienteResponseDTO);

            // Act
            cadastroClienteService.cadastrarCliente(clienteEntity);

            // Assert
            verify(passwordEncoder).encode(senhaOriginal);
            verify(clienteRepository).save(clienteEntityCaptor.capture());
            assertThat(clienteEntityCaptor.getValue().getSenha()).isEqualTo(senhaCriptografada);
        }

        @Test
        @DisplayName("Deve definir perfil como CLIENTE")
        void deveDefinirPerfilComoCliente() {
            // Arrange
            String senhaCriptografada = "senhaCriptografada";
            ClienteEntity clienteSalvo = criarClienteSalvo(senhaCriptografada);

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(anyString())).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);
            when(clienteMapper.toDTO(any(ClienteEntity.class))).thenReturn(cadastroFeignClientRequestDTO);
            when(clienteFeignClient.cadastrarCliente(any(CadastroFeignClientRequestDTO.class))).thenReturn(clienteResponseDTO);

            // Act
            cadastroClienteService.cadastrarCliente(clienteEntity);

            // Assert
            verify(clienteRepository).save(clienteEntityCaptor.capture());
            assertThat(clienteEntityCaptor.getValue().getPerfil()).isEqualTo(Perfil.CLIENTE);
        }

        @Test
        @DisplayName("Deve chamar Feign Client após salvar no banco")
        void deveChamarFeignClientAposSalvar() {
            // Arrange
            String senhaCriptografada = "senhaCriptografada";
            ClienteEntity clienteSalvo = criarClienteSalvo(senhaCriptografada);

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(anyString())).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);
            when(clienteMapper.toDTO(any(ClienteEntity.class))).thenReturn(cadastroFeignClientRequestDTO);
            when(clienteFeignClient.cadastrarCliente(any(CadastroFeignClientRequestDTO.class))).thenReturn(clienteResponseDTO);

            // Act
            cadastroClienteService.cadastrarCliente(clienteEntity);

            // Assert
            verify(clienteFeignClient).cadastrarCliente(cadastroFeignClientRequestDTOCaptor.capture());
            assertThat(cadastroFeignClientRequestDTOCaptor.getValue()).isEqualTo(cadastroFeignClientRequestDTO);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class CenariosValidacao {

        @Test
        @DisplayName("Não deve salvar cliente quando validação falha")
        void naoDeveSalvarQuandoValidacaoFalha() {
            // Arrange
            RuntimeException excecaoValidacao = new RuntimeException("Erro de validação");
            doThrow(excecaoValidacao).when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteService.cadastrarCliente(clienteEntity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro de validação");

            verify(clienteRepository, never()).save(any(ClienteEntity.class));
            verify(clienteFeignClient, never()).cadastrarCliente(any(CadastroFeignClientRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {

        @Test
        @DisplayName("Deve propagar exceção quando repositório falha")
        void devePropararExcecaoQuandoRepositorioFalha() {
            // Arrange
            String senhaCriptografada = "senhaCriptografada";

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(anyString())).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class)))
                    .thenThrow(new RuntimeException("Erro de banco de dados"));

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteService.cadastrarCliente(clienteEntity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro de banco de dados");

            verify(clienteFeignClient, never()).cadastrarCliente(any(CadastroFeignClientRequestDTO.class));
        }

        @Test
        @DisplayName("Deve propagar exceção quando Feign Client falha")
        void devePropagarExcecaoQuandoFeignClientFalha() {
            // Arrange
            String senhaCriptografada = "senhaCriptografada";
            ClienteEntity clienteSalvo = criarClienteSalvo(senhaCriptografada);

            doNothing().when(cadastroClienteValidation).validarCadastroCliente(any(ClienteEntity.class));
            when(passwordEncoder.encode(anyString())).thenReturn(senhaCriptografada);
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);
            when(clienteMapper.toDTO(any(ClienteEntity.class))).thenReturn(cadastroFeignClientRequestDTO);
            when(clienteFeignClient.cadastrarCliente(any(CadastroFeignClientRequestDTO.class)))
                    .thenThrow(new RuntimeException("Serviço indisponível"));

            // Act & Assert
            assertThatThrownBy(() -> cadastroClienteService.cadastrarCliente(clienteEntity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Serviço indisponível");
        }
    }

    private ClienteEntity criarClienteSalvo(String senhaCriptografada) {
        ClienteEntity clienteSalvo = new ClienteEntity();
        clienteSalvo.setId(UUID.randomUUID());
        clienteSalvo.setNome("João Silva");
        clienteSalvo.setEmail("joao@example.com");
        clienteSalvo.setSenha(senhaCriptografada);
        clienteSalvo.setSaldo(500.0);
        clienteSalvo.setPerfil(Perfil.CLIENTE);
        return clienteSalvo;
    }
}

