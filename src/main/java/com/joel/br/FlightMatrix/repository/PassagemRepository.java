package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PassagemRepository extends JpaRepository<Passagem, Long> {

    List<Passagem> findByOrigemAndDestino(Aeroporto origem, Aeroporto destino);

    List<Passagem> findByOrigemAndDestinoAndDataIdaBetween(
            Aeroporto origem,
            Aeroporto destino,
            LocalDate dataIdaInicio,
            LocalDate dataIdaFim
    );

    List<Passagem> findByOrigemAndDestinoAndDataIdaBetweenAndDataVoltaBetween(
            Aeroporto origem,
            Aeroporto destino,
            LocalDate dataIdaInicio,
            LocalDate dataIdaFim,
            LocalDate dataVoltaInicio,
            LocalDate dataVoltaFim
    );

    @Query("SELECT p FROM Passagem p WHERE p.origem = :origem AND p.destino = :destino " +
            "AND p.dataIda BETWEEN :dataIdaInicio AND :dataIdaFim " +
            "AND p.preco <= :precoMaximo ORDER BY p.preco ASC")
    List<Passagem> buscarPassagensPorPreco(
            Aeroporto origem,
            Aeroporto destino,
            LocalDate dataIdaInicio,
            LocalDate dataIdaFim,
            BigDecimal precoMaximo
    );

    List<Passagem> findByCompanhiaAereaAndOrigemAndDestino(
            String companhiaAerea,
            Aeroporto origem,
            Aeroporto destino
    );

    List<Passagem> findByFonte(FontePassagem fonte);

    List<Passagem> findByOrigemCodigoAndDestinoCodigoAndDataIdaBetween(String codigoOrigem, String codigoDestino, LocalDate dataInicio, LocalDate dataFim);
}
