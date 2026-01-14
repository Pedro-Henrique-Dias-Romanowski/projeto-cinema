package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Sessao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessaoService {

    public Sessao fazerReservaSessao(Sessao sessao){
        return null;
    }

    public Sessao confirmarReservaSessao(Long id){
        return null;
    }

    public List<Sessao> listarSessoes(){
        return List.of();
    }

    public Optional<Sessao> procurarSessaoPorId(Long id){
        return Optional.empty();
    }

    public void cancelarReservaSessao(Long id){

    }
}
