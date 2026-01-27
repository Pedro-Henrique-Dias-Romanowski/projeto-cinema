package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
import com.romanowski.pedro.exceptions.FilmeInexistenteException;
import com.romanowski.pedro.exceptions.ListaFilmesVaziaException;
import com.romanowski.pedro.repository.FilmeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilmeValidation {

    private static final Logger logger = LoggerFactory.getLogger(FilmeValidation.class);

    private final FilmeRepository filmeRepository;

    @Value("${mensagem.filme.existente}")
    private String mensagemFilmeExistente;

    @Value("${mensagem.lista.filmes.vazia}")
    private String mensagemListaFilmesVazia;

    @Value("{${mensagem.filme.inexistente}")
    private String mensagemFilmeInexistente;

    public FilmeValidation(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    public void validarCadastroFilme(Filme filme){
        if (filmeRepository.findByTitulo(filme.getTitulo()).isPresent()){
            logger.error("Filme com o título {} já existe", filme.getTitulo());
            throw new FilmeExistenteException(mensagemFilmeExistente);
        }
    }

    public void validarListagemFilmes(){
        if (filmeRepository.findAll().isEmpty()){
            logger.error("Lista de filmes está vazia");
            throw new ListaFilmesVaziaException(mensagemListaFilmesVazia);
        }
    }

    public void validarBuscaPorFilme(Long id){
        if (filmeRepository.findById(id).isEmpty()){
            logger.error("Filme com id {} não encontrado", id);
            throw new FilmeInexistenteException(mensagemFilmeInexistente);
        }
    }

    public void validarBuscaPorFilmePeloTitulo(String titulo){
        if (filmeRepository.findByTitulo(titulo).isEmpty()){
            logger.error("Filme com título {} não encontrado", titulo);
            throw new FilmeInexistenteException(mensagemFilmeInexistente);
        }
    }


}
