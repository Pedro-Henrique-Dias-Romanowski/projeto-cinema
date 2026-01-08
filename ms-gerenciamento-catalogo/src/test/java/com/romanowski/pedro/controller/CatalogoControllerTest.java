package com.romanowski.pedro.controller;

import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Filme;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        // Preparando dados de teste
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
                "O Poderoso Chefão",
                175,
                "Drama",
                "Francis Ford Coppola",
                LocalDate.of(1972, 3, 24)
        );
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
}

