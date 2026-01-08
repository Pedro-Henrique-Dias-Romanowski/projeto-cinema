package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
import com.romanowski.pedro.exceptions.ListaFilmesVaziaException;
import com.romanowski.pedro.repository.FilmeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FilmeValidation")
class FilmeValidationTest {

    @Mock
    private FilmeRepository filmeRepository;

    @InjectMocks
    private FilmeValidation filmeValidation;

    private Filme filme;
    private Filme filmeExistente;

    // Dados para testes de listagem
    private List<Filme> listaFilmes;

    @BeforeEach
    void setUp() {
        // Injetando as mensagens de erro através de reflection (simulando @Value)
        ReflectionTestUtils.setField(filmeValidation, "mensagemFilmeExistente", "Filme já cadastrado no sistema");
        ReflectionTestUtils.setField(filmeValidation, "mensagemListaFilmesVazia", "Nenhum filme encontrado no sistema");

        // Preparando dados de teste
        filme = new Filme();
        filme.setTitulo("O Poderoso Chefão");
        filme.setDuracao(175);
        filme.setGenero("Drama");
        filme.setAutor("Francis Ford Coppola");
        filme.setDataLancamento(LocalDate.of(1972, 3, 24));

        filmeExistente = new Filme();
        filmeExistente.setId(1L);
        filmeExistente.setTitulo("O Poderoso Chefão");
        filmeExistente.setDuracao(175);
        filmeExistente.setGenero("Drama");
        filmeExistente.setAutor("Francis Ford Coppola");
        filmeExistente.setDataLancamento(LocalDate.of(1972, 3, 24));

        // Preparando lista de filmes para testes de listagem
        listaFilmes = new ArrayList<>();
        Filme filme1 = new Filme();
        filme1.setId(1L);
        filme1.setTitulo("O Poderoso Chefão");
        listaFilmes.add(filme1);

        Filme filme2 = new Filme();
        filme2.setId(2L);
        filme2.setTitulo("Interestelar");
        listaFilmes.add(filme2);

        Filme filme3 = new Filme();
        filme3.setId(3L);
        filme3.setTitulo("Matrix");
        listaFilmes.add(filme3);
    }

    @Test
    @DisplayName("Deve validar filme com sucesso quando o título não existe")
    void deveValidarFilmeComSucesso() {
        // Arrange
        when(filmeRepository.findByTitulo(filme.getTitulo())).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> filmeValidation.validarCadastroFilme(filme));
        verify(filmeRepository, times(1)).findByTitulo(filme.getTitulo());
    }

    @Test
    @DisplayName("Deve lançar FilmeExistenteException quando o título já existe")
    void deveLancarExcecaoQuandoTituloJaExiste() {
        // Arrange
        when(filmeRepository.findByTitulo(filme.getTitulo())).thenReturn(Optional.of(filmeExistente));

        // Act & Assert
        FilmeExistenteException exception = assertThrows(
                FilmeExistenteException.class,
                () -> filmeValidation.validarCadastroFilme(filme)
        );

        assertEquals("Filme já cadastrado no sistema", exception.getMessage());
        verify(filmeRepository, times(1)).findByTitulo(filme.getTitulo());
    }

    @Test
    @DisplayName("Deve validar filme com título único")
    void deveValidarFilmeComTituloUnico() {
        // Arrange
        when(filmeRepository.findByTitulo("Interestelar")).thenReturn(Optional.empty());

        Filme novoFilme = new Filme();
        novoFilme.setTitulo("Interestelar");
        novoFilme.setDuracao(169);
        novoFilme.setGenero("Ficção Científica");
        novoFilme.setAutor("Christopher Nolan");
        novoFilme.setDataLancamento(LocalDate.of(2014, 11, 7));

        // Act & Assert
        assertDoesNotThrow(() -> filmeValidation.validarCadastroFilme(novoFilme));
        verify(filmeRepository, times(1)).findByTitulo("Interestelar");
    }

    @Test
    @DisplayName("Deve lançar exceção com mensagem correta quando filme existe")
    void deveLancarExcecaoComMensagemCorreta() {
        // Arrange
        when(filmeRepository.findByTitulo(filme.getTitulo())).thenReturn(Optional.of(filmeExistente));

        // Act & Assert
        FilmeExistenteException exception = assertThrows(
                FilmeExistenteException.class,
                () -> filmeValidation.validarCadastroFilme(filme)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("cadastrado"));
    }

    @Test
    @DisplayName("Deve validar diferentes títulos de filmes únicos")
    void deveValidarDiferentesTitulosUnicos() {
        // Arrange
        Filme filme1 = new Filme();
        filme1.setTitulo("Matrix");
        filme1.setDuracao(136);
        filme1.setGenero("Ficção Científica");
        filme1.setAutor("Wachowski");
        filme1.setDataLancamento(LocalDate.of(1999, 3, 31));

        Filme filme2 = new Filme();
        filme2.setTitulo("Inception");
        filme2.setDuracao(148);
        filme2.setGenero("Ficção Científica");
        filme2.setAutor("Christopher Nolan");
        filme2.setDataLancamento(LocalDate.of(2010, 7, 16));

        when(filmeRepository.findByTitulo("Matrix")).thenReturn(Optional.empty());
        when(filmeRepository.findByTitulo("Inception")).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> filmeValidation.validarCadastroFilme(filme1));
        assertDoesNotThrow(() -> filmeValidation.validarCadastroFilme(filme2));
        verify(filmeRepository, times(1)).findByTitulo("Matrix");
        verify(filmeRepository, times(1)).findByTitulo("Inception");
    }

    @Test
    @DisplayName("Deve validar listagem com sucesso quando há filmes cadastrados")
    void deveValidarListagemComSucesso() {
        // Arrange
        when(filmeRepository.findAll()).thenReturn(listaFilmes);

        // Act & Assert
        assertDoesNotThrow(() -> filmeValidation.validarListagemClientes());
        verify(filmeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve lançar ListaFilmesVaziaException quando não há filmes cadastrados")
    void deveLancarExcecaoQuandoListaVazia() {
        // Arrange
        when(filmeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ListaFilmesVaziaException exception = assertThrows(
                ListaFilmesVaziaException.class,
                () -> filmeValidation.validarListagemClientes()
        );

        assertEquals("Nenhum filme encontrado no sistema", exception.getMessage());
        verify(filmeRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Deve lançar exceção com mensagem correta quando lista está vazia")
    void deveLancarExcecaoComMensagemCorretaParaListagem() {
        // Arrange
        when(filmeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ListaFilmesVaziaException exception = assertThrows(
                ListaFilmesVaziaException.class,
                () -> filmeValidation.validarListagemClientes()
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Nenhum filme"));
    }


    @Test
    @DisplayName("Deve lançar exceção quando lista é null tratada como vazia")
    void deveLancarExcecaoQuandoListaNullTratadaComoVazia() {
        // Arrange
        when(filmeRepository.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(ListaFilmesVaziaException.class,
            () -> filmeValidation.validarListagemClientes());
        verify(filmeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve validar múltiplas chamadas consecutivas com sucesso")
    void deveValidarMultiplasChamas() {
        // Arrange
        when(filmeRepository.findAll()).thenReturn(listaFilmes);

        // Act & Assert
        assertDoesNotThrow(() -> filmeValidation.validarListagemClientes());
        assertDoesNotThrow(() -> filmeValidation.validarListagemClientes());
        assertDoesNotThrow(() -> filmeValidation.validarListagemClientes());

        verify(filmeRepository, times(3)).findAll();
    }


    @Test
    @DisplayName("Não deve modificar a lista de filmes durante validação")
    void naoDeveModificarListaDuranteValidacao() {
        // Arrange
        List<Filme> listaOriginal = new ArrayList<>(listaFilmes);
        int tamanhoOriginal = listaOriginal.size();
        when(filmeRepository.findAll()).thenReturn(listaOriginal);

        // Act
        filmeValidation.validarListagemClientes();

        // Assert
        assertEquals(tamanhoOriginal, listaOriginal.size());
    }
}


