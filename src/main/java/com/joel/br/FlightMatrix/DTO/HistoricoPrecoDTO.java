package com.joel.br.FlightMatrix.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPrecoDTO {

    private Long id;
    private String origem;
    private String destino;
    private String companhiaAerea;
    private BigDecimal preco;
    private LocalDateTime dataHoraConsulta;
    private String moeda;
    private String fonte;
}