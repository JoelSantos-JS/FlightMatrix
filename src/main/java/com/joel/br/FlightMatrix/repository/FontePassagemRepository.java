package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.FontePassagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FontePassagemRepository extends JpaRepository<FontePassagem,Long > {

    public List<FontePassagem> findByAtiva(Boolean ativa);
    
    public Optional<FontePassagem> findByNome(String nome);
}
