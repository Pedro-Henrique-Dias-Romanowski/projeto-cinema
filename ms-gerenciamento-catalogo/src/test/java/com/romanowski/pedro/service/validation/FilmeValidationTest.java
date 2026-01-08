package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmeValidationTest {

    @Mock
    private FilmeRepository filmeRepository;

    @InjectMocks
    private FilmeValidation filmeValidation;

    private Filme filme;
    private Filme filmeExistente;

    @BeforeEach
    void setUp() {
        // Injetando a mensagem de erro através de reflection (simulando @Value)
        ReflectionTestUtils.setField(filmeValidation, "mensagemFilmeExistente", "Filme já cadastrado no sistema");

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

}

