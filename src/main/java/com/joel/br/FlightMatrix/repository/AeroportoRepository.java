package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Aeroporto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AeroportoRepository extends JpaRepository<Aeroporto, String> {
    List<Aeroporto> findByCidadeContainingIgnoreCase(String cidade);
    List<Aeroporto> findByPaisContainingIgnoreCase(String pais);
}
