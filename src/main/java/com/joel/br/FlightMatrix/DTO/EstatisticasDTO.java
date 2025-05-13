package com.joel.br.FlightMatrix.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasDTO {

    private String origem;
    private String destino;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal precoMedio;
    private BigDecimal precoMinimo;
    private BigDecimal precoMaximo;
    private Map<String, BigDecimal> precoMedioPorCompanhia;
    private List<HistoricoPrecoDTO> historicoDiario;
}