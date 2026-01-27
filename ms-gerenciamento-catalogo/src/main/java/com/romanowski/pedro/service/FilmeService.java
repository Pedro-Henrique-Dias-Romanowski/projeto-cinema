package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.repository.FilmeRepository;
import com.romanowski.pedro.service.validation.FilmeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FilmeService {

    private static final Logger logger = LoggerFactory.getLogger(FilmeService.class.getName());

    private final FilmeValidation filmeValidation;
    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeValidation filmeValidation, FilmeRepository filmeRepository) {
        this.filmeValidation = filmeValidation;
        this.filmeRepository = filmeRepository;
    }


    public Filme cadastrarFilme(Filme filme){
        logger.info("Iniciando processo de cadastro de filme");
        filmeValidation.validarCadastroFilme(filme);
        return filmeRepository.save(filme);
    }

    public List<Filme> listarFilmes(){
        logger.info("Iniciando processo de listagem de filmes");
        filmeValidation.validarListagemFilmes();
        return filmeRepository.findAll();
    }

    public Optional<Filme> buscarFilmePorId(Long id){
        logger.info("Iniciando processo de busca de filme por id: {}", id);
        filmeValidation.validarBuscaPorFilme(id);
        return filmeRepository.findById(id);
    }

    public Optional<Filme> buscarFilmePorTitulo(String titulo){
        logger.info("Iniciando processo de busca de filme por título: {}", titulo);
        filmeValidation.validarBuscaPorFilmePeloTitulo(titulo);
        return filmeRepository.findByTitulo(titulo);
    }

    public Filme atualizarFilme(Long id, Filme filme){
        logger.info("Iniciando processo de atualização de filme com id: {}", id);
        filmeValidation.validarBuscaPorFilme(id);
        Filme filmeExistente = filmeRepository.findById(id).get();
        filme.setId(filmeExistente.getId());
        return filmeRepository.save(filme);
    }

    public void deletarFilme(Long id){
        logger.info("Iniciando processo de remoção de filme com id: {}", id);
        filmeValidation.validarBuscaPorFilme(id);
        filmeRepository.deleteById(id);
    }
}
