package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.repository.FilmeRepository;
import com.romanowski.pedro.service.validation.FilmeValidation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FilmeService {

    private final FilmeValidation filmeValidation;
    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeValidation filmeValidation, FilmeRepository filmeRepository) {
        this.filmeValidation = filmeValidation;
        this.filmeRepository = filmeRepository;
    }


    public Filme cadastrarFilme(Filme filme){
        filmeValidation.validarCadastroFilme(filme);
        return filmeRepository.save(filme);
    }

    public List<Filme> listarFilmes(){
        filmeValidation.validarListagemClientes();
        return filmeRepository.findAll();
    }

    public Optional<Filme> buscarFilmePorId(Long id){
        filmeValidation.validarBuscaPorFilme(id);
        return filmeRepository.findById(id);
    }

    public Optional<Filme> buscarFilmePorTitulo(String titulo){
        filmeValidation.validarBuscaPorFilmePeloTitulo(titulo);
        return filmeRepository.findByTitulo(titulo);
    }

    public Filme atualizarFilme(Long id, Filme filme){
        filmeValidation.validarBuscaPorFilme(id);
        Filme filmeExistente = filmeRepository.findById(id).get();
        filme.setId(filmeExistente.getId());
        return filmeRepository.save(filme);
    }

    public void deletarFilme(Long id){
        filmeValidation.validarBuscaPorFilme(id);
        filmeRepository.deleteById(id);
    }
}
