package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
import com.romanowski.pedro.exceptions.ListaFilmesVaziaException;
import com.romanowski.pedro.repository.FilmeRepository;
import com.romanowski.pedro.service.validation.FilmeValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FilmeService")
class FilmeServiceTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private FilmeValidation filmeValidation;

    @InjectMocks
    private FilmeService filmeService;

    private Filme filme;
    private Filme filmeSalvo;

    // Dados para testes de listagem
    private List<Filme> listaFilmes;
    private Filme filme1;
    private Filme filme2;
    private Filme filme3;

    @BeforeEach
    void setUp() {
        // Preparando dados de teste para cadastro
        filme = new Filme();
        filme.setTitulo("O Poderoso Chefão");
        filme.setDuracao(175);
        filme.setGenero("Drama");
        filme.setAutor("Francis Ford Coppola");
        filme.setDataLancamento(LocalDate.of(1972, 3, 24));

        filmeSalvo = new Filme();
        filmeSalvo.setId(1L);
        filmeSalvo.setTitulo("O Poderoso Chefão");
        filmeSalvo.setDuracao(175);
        filmeSalvo.setGenero("Drama");
        filmeSalvo.setAutor("Francis Ford Coppola");
        filmeSalvo.setDataLancamento(LocalDate.of(1972, 3, 24));

        // Preparando dados de teste para listagem - Filme 1
        filme1 = new Filme();
        filme1.setId(1L);
        filme1.setTitulo("O Poderoso Chefão");
        filme1.setDuracao(175);
        filme1.setGenero("Drama");
        filme1.setAutor("Francis Ford Coppola");
        filme1.setDataLancamento(LocalDate.of(1972, 3, 24));

        // Filme 2
        filme2 = new Filme();
        filme2.setId(2L);
        filme2.setTitulo("Interestelar");
        filme2.setDuracao(169);
        filme2.setGenero("Ficção Científica");
        filme2.setAutor("Christopher Nolan");
        filme2.setDataLancamento(LocalDate.of(2014, 11, 7));

        // Filme 3
        filme3 = new Filme();
        filme3.setId(3L);
        filme3.setTitulo("Matrix");
        filme3.setDuracao(136);
        filme3.setGenero("Ficção Científica");
        filme3.setAutor("Wachowski");
        filme3.setDataLancamento(LocalDate.of(1999, 3, 31));

        // Preparando lista
        listaFilmes = new ArrayList<>();
        listaFilmes.add(filme1);
        listaFilmes.add(filme2);
        listaFilmes.add(filme3);
    }

    @Test
    @DisplayName("Deve cadastrar um filme com sucesso quando todos os dados são válidos")
    void deveCadastrarFilmeComSucesso() {
        // Arrange
        doNothing().when(filmeValidation).validarCadastroFilme(filme);
        when(filmeRepository.save(filme)).thenReturn(filmeSalvo);

        // Act
        Filme resultado = filmeService.cadastrarFilme(filme);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("O Poderoso Chefão", resultado.getTitulo());
        assertEquals(175, resultado.getDuracao());
        assertEquals("Drama", resultado.getGenero());
        assertEquals("Francis Ford Coppola", resultado.getAutor());
        assertEquals(LocalDate.of(1972, 3, 24), resultado.getDataLancamento());

        verify(filmeValidation, times(1)).validarCadastroFilme(filme);
        verify(filmeRepository, times(1)).save(filme);
    }

    @Test
    @DisplayName("Deve lançar FilmeExistenteException quando o filme já existe")
    void deveLancarExcecaoQuandoFilmeJaExiste() {
        // Arrange
        doThrow(new FilmeExistenteException("Filme já cadastrado no sistema"))
                .when(filmeValidation).validarCadastroFilme(filme);

        // Act & Assert
        FilmeExistenteException exception = assertThrows(
                FilmeExistenteException.class,
                () -> filmeService.cadastrarFilme(filme)
        );

        assertEquals("Filme já cadastrado no sistema", exception.getMessage());
        verify(filmeValidation, times(1)).validarCadastroFilme(filme);
        verify(filmeRepository, never()).save(any(Filme.class));
    }

    @Test
    @DisplayName("Não deve salvar o filme quando a validação falha")
    void naoDeveSalvarQuandoValidacaoFalha() {
        // Arrange
        doThrow(new FilmeExistenteException("Filme já cadastrado no sistema"))
                .when(filmeValidation).validarCadastroFilme(filme);

        // Act & Assert
        assertThrows(FilmeExistenteException.class, () -> filmeService.cadastrarFilme(filme));
        verify(filmeRepository, never()).save(any(Filme.class));
    }

    @Test
    @DisplayName("Deve retornar lista com todos os filmes cadastrados")
    void deveRetornarListaComTodosOsFilmes() {
        // Arrange
        doNothing().when(filmeValidation).validarListagemClientes();
        when(filmeRepository.findAll()).thenReturn(listaFilmes);

        // Act
        List<Filme> resultado = filmeService.listarFilmes();

        // Assert
        assertNotNull(resultado);
        assertEquals(listaFilmes.size(), resultado.size());
        assertTrue(resultado.contains(filme1));
        assertTrue(resultado.contains(filme2));
        assertTrue(resultado.contains(filme3));
    }

    @Test
    @DisplayName("Deve lançar ListaFilmesVaziaException quando não há filmes cadastrados")
    void deveLancarExcecaoQuandoNaoHaFilmes() {
        // Arrange
        doThrow(new ListaFilmesVaziaException("Nenhum filme encontrado no sistema"))
                .when(filmeValidation).validarListagemClientes();

        // Act & Assert
        ListaFilmesVaziaException exception = assertThrows(
                ListaFilmesVaziaException.class,
                () -> filmeService.listarFilmes()
        );

        assertEquals("Nenhum filme encontrado no sistema", exception.getMessage());
        verify(filmeValidation, times(1)).validarListagemClientes();
        verify(filmeRepository, never()).findAll();
    }

    @Test
    @DisplayName("Não deve buscar filmes no repositório quando a validação falha")
    void naoDeveBuscarFilmesQuandoValidacaoFalha() {
        // Arrange
        doThrow(new ListaFilmesVaziaException("Nenhum filme encontrado no sistema"))
                .when(filmeValidation).validarListagemClientes();

        // Act & Assert
        assertThrows(ListaFilmesVaziaException.class, () -> filmeService.listarFilmes());
        verify(filmeRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve retornar filmes com todos os dados corretos")
    void deveRetornarFilmesComTodosDadosCorretos() {
        // Arrange
        doNothing().when(filmeValidation).validarListagemClientes();
        when(filmeRepository.findAll()).thenReturn(listaFilmes);

        // Act
        List<Filme> resultado = filmeService.listarFilmes();

        // Assert
        assertNotNull(resultado);

        // Verificar primeiro filme
        Filme primeiroFilme = resultado.get(0);
        assertEquals(1L, primeiroFilme.getId());
        assertEquals("O Poderoso Chefão", primeiroFilme.getTitulo());
        assertEquals(175, primeiroFilme.getDuracao());
        assertEquals("Drama", primeiroFilme.getGenero());
        assertEquals("Francis Ford Coppola", primeiroFilme.getAutor());

        // Verificar segundo filme
        Filme segundoFilme = resultado.get(1);
        assertEquals(2L, segundoFilme.getId());
        assertEquals("Interestelar", segundoFilme.getTitulo());
        assertEquals(169, segundoFilme.getDuracao());
        assertEquals("Ficção Científica", segundoFilme.getGenero());
        assertEquals("Christopher Nolan", segundoFilme.getAutor());

        // Verificar terceiro filme
        Filme terceiroFilme = resultado.get(2);
        assertEquals(3L, terceiroFilme.getId());
        assertEquals("Matrix", terceiroFilme.getTitulo());
        assertEquals(136, terceiroFilme.getDuracao());
        assertEquals("Ficção Científica", terceiroFilme.getGenero());
        assertEquals("Wachowski", terceiroFilme.getAutor());
    }

    @Test
    @DisplayName("Deve chamar validação apenas uma vez por listagem")
    void deveChamarValidacaoApenasUmaVezParaListagem() {
        // Arrange
        doNothing().when(filmeValidation).validarListagemClientes();
        when(filmeRepository.findAll()).thenReturn(listaFilmes);

        // Act
        filmeService.listarFilmes();

        // Assert
        verify(filmeValidation, times(1)).validarListagemClientes();
    }
}


