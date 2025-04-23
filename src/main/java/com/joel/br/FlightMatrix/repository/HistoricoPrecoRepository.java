package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {
    List<HistoricoPreco> findByOrigemAndDestinoAndCompanhiaAerea(
            Aeroporto origem,
            Aeroporto destino,
            String companhiaAerea
    );

    @Query("SELECT h FROM HistoricoPreco h WHERE h.origem = :origem AND h.destino = :destino " +
            "AND h.dataHoraConsulta BETWEEN :inicio AND :fim ORDER BY h.dataHoraConsulta")
    List<HistoricoPreco> findHistoricoPeriodicoByRota(
            Aeroporto origem,
            Aeroporto destino,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}
