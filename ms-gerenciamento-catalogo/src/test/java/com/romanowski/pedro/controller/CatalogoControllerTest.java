package com.romanowski.pedro.controller;

import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeInexistenteException;
import com.romanowski.pedro.exceptions.ListaFilmesVaziaException;
import com.romanowski.pedro.mapper.FilmeMapper;
import com.romanowski.pedro.service.FilmeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CatalogoController")
class CatalogoControllerTest {

    @Mock
    private FilmeService filmeService;

    @Mock
    private FilmeMapper filmeMapper;

    @InjectMocks
    private CatalogoController catalogoController;

    private FilmeRequestDTO filmeRequestDTO;
    private Filme filmeEntity;
    private Filme filmeSalvo;
    private FilmeResponseDTO filmeResponseDTO;

    // Dados para testes de listagem
    private List<Filme> listaFilmes;
    private List<FilmeResponseDTO> listaFilmesResponseDTO;
    private Filme filme1;
    private Filme filme2;
    private Filme filme3;
    private FilmeResponseDTO filmeResponseDTO1;
    private FilmeResponseDTO filmeResponseDTO2;
    private FilmeResponseDTO filmeResponseDTO3;

    @BeforeEach
    void setUp() {
        // Preparando dados de teste para cadastro
        filmeRequestDTO = new FilmeRequestDTO(
                "O Poderoso Chefão",
                175,
                "Drama",
                "Francis Ford Coppola",
                LocalDate.of(1972, 3, 24)
        );

        filmeEntity = new Filme();
        filmeEntity.setTitulo("O Poderoso Chefão");
        filmeEntity.setDuracao(175);
        filmeEntity.setGenero("Drama");
        filmeEntity.setAutor("Francis Ford Coppola");
        filmeEntity.setDataLancamento(LocalDate.of(1972, 3, 24));

        filmeSalvo = new Filme();
        filmeSalvo.setId(1L);
        filmeSalvo.setTitulo("O Poderoso Chefão");
        filmeSalvo.setDuracao(175);
        filmeSalvo.setGenero("Drama");
        filmeSalvo.setAutor("Francis Ford Coppola");
        filmeSalvo.setDataLancamento(LocalDate.of(1972, 3, 24));

        filmeResponseDTO = new FilmeResponseDTO(
                1L,
                "O Poderoso Chefão",
                175,
                "Drama",
                "Francis Ford Coppola",
                LocalDate.of(1972, 3, 24)
        );

        // Preparando dados de teste para listagem - Filme 1
        filme1 = new Filme();
        filme1.setId(1L);
        filme1.setTitulo("O Poderoso Chefão");
        filme1.setDuracao(175);
        filme1.setGenero("Drama");
        filme1.setAutor("Francis Ford Coppola");
        filme1.setDataLancamento(LocalDate.of(1972, 3, 24));

        filmeResponseDTO1 = new FilmeResponseDTO(
                1L,
                "O Poderoso Chefão",
                175,
                "Drama",
                "Francis Ford Coppola",
                LocalDate.of(1972, 3, 24)
        );

        // Preparando dados de teste - Filme 2
        filme2 = new Filme();
        filme2.setId(2L);
        filme2.setTitulo("Interestelar");
        filme2.setDuracao(169);
        filme2.setGenero("Ficção Científica");
        filme2.setAutor("Christopher Nolan");
        filme2.setDataLancamento(LocalDate.of(2014, 11, 7));

        filmeResponseDTO2 = new FilmeResponseDTO(
                2L,
                "Interestelar",
                169,
                "Ficção Científica",
                "Christopher Nolan",
                LocalDate.of(2014, 11, 7)
        );

        // Preparando dados de teste - Filme 3
        filme3 = new Filme();
        filme3.setId(3L);
        filme3.setTitulo("Matrix");
        filme3.setDuracao(136);
        filme3.setGenero("Ficção Científica");
        filme3.setAutor("Wachowski");
        filme3.setDataLancamento(LocalDate.of(1999, 3, 31));

        filmeResponseDTO3 = new FilmeResponseDTO(
                3L,
                "Matrix",
                136,
                "Ficção Científica",
                "Wachowski",
                LocalDate.of(1999, 3, 31)
        );

        // Preparando listas
        listaFilmes = new ArrayList<>();
        listaFilmes.add(filme1);
        listaFilmes.add(filme2);
        listaFilmes.add(filme3);

        listaFilmesResponseDTO = new ArrayList<>();
        listaFilmesResponseDTO.add(filmeResponseDTO1);
        listaFilmesResponseDTO.add(filmeResponseDTO2);
        listaFilmesResponseDTO.add(filmeResponseDTO3);
    }

    @Test
    @DisplayName("Deve cadastrar um filme com sucesso e retornar status 201 CREATED")
    void deveCadastrarFilmeComSucesso() {
        // Arrange (preparar)
        when(filmeMapper.toEntity(filmeRequestDTO)).thenReturn(filmeEntity);
        when(filmeService.cadastrarFilme(filmeEntity)).thenReturn(filmeSalvo);
        when(filmeMapper.toResponseDTO(filmeSalvo)).thenReturn(filmeResponseDTO);

        // Act (agir)
        ResponseEntity<FilmeResponseDTO> response = catalogoController.cadastrarFilme(filmeRequestDTO);

        // Assert (verificar)
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("O Poderoso Chefão", response.getBody().titulo());
        assertEquals(175, response.getBody().duracao());
        assertEquals("Drama", response.getBody().genero());
        assertEquals("Francis Ford Coppola", response.getBody().autor());
        assertEquals(LocalDate.of(1972, 3, 24), response.getBody().dataLancamento());

        // Verificar que os métodos foram chamados
        verify(filmeMapper, times(1)).toEntity(filmeRequestDTO);
        verify(filmeService, times(1)).cadastrarFilme(filmeEntity);
        verify(filmeMapper, times(1)).toResponseDTO(filmeSalvo);
    }

    @Test
    @DisplayName("Deve retornar o filme cadastrado no corpo da resposta")
    void deveRetornarFilmeNaResposta() {
        // Arrange
        when(filmeMapper.toEntity(any(FilmeRequestDTO.class))).thenReturn(filmeEntity);
        when(filmeService.cadastrarFilme(any(Filme.class))).thenReturn(filmeSalvo);
        when(filmeMapper.toResponseDTO(any(Filme.class))).thenReturn(filmeResponseDTO);

        // Act
        ResponseEntity<FilmeResponseDTO> response = catalogoController.cadastrarFilme(filmeRequestDTO);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(filmeResponseDTO, response.getBody());
    }

    @Test
    @DisplayName("Deve listar todos os filmes com sucesso e retornar status 200 OK")
    void deveListarTodosOsFilmesComSucesso() {
        // Arrange
        when(filmeService.listarFilmes()).thenReturn(listaFilmes);
        when(filmeMapper.toResponseDTO(filme1)).thenReturn(filmeResponseDTO1);
        when(filmeMapper.toResponseDTO(filme2)).thenReturn(filmeResponseDTO2);
        when(filmeMapper.toResponseDTO(filme3)).thenReturn(filmeResponseDTO3);

        // Act
        ResponseEntity<List<FilmeResponseDTO>> response = catalogoController.listarFilmes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        verify(filmeService, times(1)).listarFilmes();
        verify(filmeMapper, times(3)).toResponseDTO(any(Filme.class));
    }

    @Test
    @DisplayName("Deve retornar filmes com todos os dados corretos")
    void deveRetornarFilmesComDadosCorretos() {
        // Arrange
        when(filmeService.listarFilmes()).thenReturn(listaFilmes);
        when(filmeMapper.toResponseDTO(filme1)).thenReturn(filmeResponseDTO1);
        when(filmeMapper.toResponseDTO(filme2)).thenReturn(filmeResponseDTO2);
        when(filmeMapper.toResponseDTO(filme3)).thenReturn(filmeResponseDTO3);

        // Act
        ResponseEntity<List<FilmeResponseDTO>> response = catalogoController.listarFilmes();

        // Assert
        List<FilmeResponseDTO> filmes = response.getBody();
        assertNotNull(filmes);

        // Verificar primeiro filme
        assertEquals(1L, filmes.get(0).id());
        assertEquals("O Poderoso Chefão", filmes.get(0).titulo());
        assertEquals(175, filmes.get(0).duracao());
        assertEquals("Drama", filmes.get(0).genero());
        assertEquals("Francis Ford Coppola", filmes.get(0).autor());

        // Verificar segundo filme
        assertEquals(2L, filmes.get(1).id());
        assertEquals("Interestelar", filmes.get(1).titulo());
        assertEquals(169, filmes.get(1).duracao());
        assertEquals("Ficção Científica", filmes.get(1).genero());
        assertEquals("Christopher Nolan", filmes.get(1).autor());

        // Verificar terceiro filme
        assertEquals(3L, filmes.get(2).id());
        assertEquals("Matrix", filmes.get(2).titulo());
        assertEquals(136, filmes.get(2).duracao());
        assertEquals("Ficção Científica", filmes.get(2).genero());
        assertEquals("Wachowski", filmes.get(2).autor());
    }

    @Test
    @DisplayName("Deve lançar ListaFilmesVaziaException quando a lista está vazia")
    void deveLancarExcecaoQuandoListaVazia() {
        // Arrange
        when(filmeService.listarFilmes()).thenThrow(new ListaFilmesVaziaException("Nenhum filme encontrado no sistema"));

        // Act & Assert
        ListaFilmesVaziaException exception = assertThrows(
                ListaFilmesVaziaException.class,
                () -> catalogoController.listarFilmes()
        );

        assertEquals("Nenhum filme encontrado no sistema", exception.getMessage());
        verify(filmeService, times(1)).listarFilmes();
        verify(filmeMapper, never()).toResponseDTO(any(Filme.class));
    }


    @Test
    @DisplayName("Deve buscar filme por ID com sucesso e retornar status 200 OK")
    void devebuscarFilmePorIdPorIdComSucesso() {
        // Arrange
        Long id = 1L;
        when(filmeService.buscarFilmePorId(id)).thenReturn(Optional.of(filmeSalvo));
        when(filmeMapper.toResponseDTO(filmeSalvo)).thenReturn(filmeResponseDTO);

        // Act
        ResponseEntity<FilmeResponseDTO> response = catalogoController.buscarFilmePorId(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("O Poderoso Chefão", response.getBody().titulo());
        assertEquals(175, response.getBody().duracao());
        assertEquals("Drama", response.getBody().genero());
        assertEquals("Francis Ford Coppola", response.getBody().autor());

        verify(filmeService, times(1)).buscarFilmePorId(id);
        verify(filmeMapper, times(1)).toResponseDTO(filmeSalvo);
    }

    @Test
    @DisplayName("Deve chamar o service para buscar filme por ID")
    void deveChamarServiceParaBuscarPorId() {
        // Arrange
        Long id = 1L;
        when(filmeService.buscarFilmePorId(id)).thenReturn(Optional.of(filmeSalvo));
        when(filmeMapper.toResponseDTO(any(Filme.class))).thenReturn(filmeResponseDTO);

        // Act
        catalogoController.buscarFilmePorId(id);

        // Assert
        verify(filmeService, times(1)).buscarFilmePorId(id);
    }

    @Test
    @DisplayName("Deve converter filme encontrado para DTO de resposta")
    void deveConverterFilmeEncontradoParaResponseDTO() {
        // Arrange
        Long id = 1L;
        when(filmeService.buscarFilmePorId(id)).thenReturn(Optional.of(filmeSalvo));
        when(filmeMapper.toResponseDTO(filmeSalvo)).thenReturn(filmeResponseDTO);

        // Act
        catalogoController.buscarFilmePorId(id);

        // Assert
        verify(filmeMapper, times(1)).toResponseDTO(filmeSalvo);
    }

    @Test
    @DisplayName("Deve retornar o filme no corpo da resposta quando encontrado")
    void deveRetornarFilmeNaRespostaQuandoEncontrado() {
        // Arrange
        Long id = 1L;
        when(filmeService.buscarFilmePorId(id)).thenReturn(Optional.of(filmeSalvo));
        when(filmeMapper.toResponseDTO(filmeSalvo)).thenReturn(filmeResponseDTO);

        // Act
        ResponseEntity<FilmeResponseDTO> response = catalogoController.buscarFilmePorId(id);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(filmeResponseDTO, response.getBody());
    }
    

    @Test
    @DisplayName("Deve processar Optional vazio corretamente")
    void deveProcessarOptionalVazioCorretamente() {
        // Arrange
        Long id = 100L;
        when(filmeService.buscarFilmePorId(id)).thenReturn(Optional.empty());
        when(filmeMapper.toResponseDTO(null)).thenReturn(null);

        // Act
        ResponseEntity<FilmeResponseDTO> response = catalogoController.buscarFilmePorId(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Deve lançar FilmeInexistenteException quando filme não é encontrado")
    void deveLancarExcecaoQuandoFilmeNaoExiste() {
        // Arrange
        Long id = 999L;
        when(filmeService.buscarFilmePorId(id)).thenThrow(new FilmeInexistenteException("Filme não encontrado no sistema"));

        // Act & Assert
        FilmeInexistenteException exception = assertThrows(
                FilmeInexistenteException.class,
                () -> catalogoController.buscarFilmePorId(id)
        );

        assertEquals("Filme não encontrado no sistema", exception.getMessage());
        verify(filmeService, times(1)).buscarFilmePorId(id);
        verify(filmeMapper, never()).toResponseDTO(any(Filme.class));
    }

}


