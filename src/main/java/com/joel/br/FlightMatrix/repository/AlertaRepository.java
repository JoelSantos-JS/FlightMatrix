package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByUsuario(Usuario usuario);

    List<Alerta> findByAtivo(Boolean ativo);

    List<Alerta> findByOrigemAndDestino(Aeroporto origem, Aeroporto destino);

    @Query("SELECT a FROM Alerta a WHERE a.ativo = true AND " +
            "(a.ultimaNotificacao IS NULL OR a.ultimaNotificacao < :threshold)")
    List<Alerta> findAlertasParaVerificar(LocalDateTime threshold);

    @Query("SELECT a FROM Alerta a WHERE a.ativo = true " +
            "AND a.origem = :origem AND a.destino = :destino " +
            "AND (:dataIda BETWEEN a.dataIdaMinima AND a.dataIdaMaxima OR a.dataIdaMinima IS NULL) " +
            "AND (:dataVolta BETWEEN a.dataVoltaMinima AND a.dataVoltaMaxima OR a.dataVoltaMinima IS NULL OR :dataVolta IS NULL)")
    List<Alerta> findAlertasCompativeis(
            Aeroporto origem,
            Aeroporto destino,
            LocalDate dataIda,
            LocalDate dataVolta
    );
}
