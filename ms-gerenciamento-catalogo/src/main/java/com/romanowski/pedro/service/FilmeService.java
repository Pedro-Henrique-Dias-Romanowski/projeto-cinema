package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.repository.FilmeRepository;
import com.romanowski.pedro.service.validation.FilmeValidation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FilmeService {

    private FilmeValidation filmeValidation;
    private FilmeRepository filmeRepository;


    public Filme cadastrarFilme(Filme filme){
        return null;
    }

    public List<Filme> listarFilmes(){
        return List.of();
    }

    public Optional<Filme> buscarFilmePorId(Long id){
        return null;
    }

    public Filme atualizarFilme(Long id, Filme filme){
        return null;
    }

    public void deletarFilme(Long id){

    }
}
