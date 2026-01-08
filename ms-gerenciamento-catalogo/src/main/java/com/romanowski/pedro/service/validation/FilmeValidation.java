package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.exceptions.FilmeExistenteException;
import com.romanowski.pedro.exceptions.ListaFilmesVaziaException;
import com.romanowski.pedro.repository.FilmeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilmeValidation {

    private final FilmeRepository filmeRepository;

    @Value("${mensagem.filme.existente}")
    private String mensagemFilmeExistente;

    @Value("${mensagem.lista.filmes.vazia}")
    private String mensagemListaFilmesVazia;

    public FilmeValidation(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    public void validarCadastroFilme(Filme filme){
        if (filmeRepository.findByTitulo(filme.getTitulo()).isPresent()){
            throw new FilmeExistenteException(mensagemFilmeExistente);
        }
    }

    public void validarListagemClientes(){
        if (filmeRepository.findAll().isEmpty()){
            throw new ListaFilmesVaziaException(mensagemListaFilmesVazia);
        }
    }


}
