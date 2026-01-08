package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private FilmeValidation filmeValidation;

    @InjectMocks
    private FilmeService filmeService;

    private Filme filme;
    private Filme filmeSalvo;

    @BeforeEach
    void setUp() {
        // Preparando dados de teste
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

}

