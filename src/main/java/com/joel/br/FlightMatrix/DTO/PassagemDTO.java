package com.joel.br.FlightMatrix.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassagemDTO {

    private Long id;
    private String origem;
    private String destino;
    private LocalDate dataIda;
    private LocalDate dataVolta;
    private BigDecimal preco;
    private BigDecimal precoAnterior;
    private String companhiaAerea;
    private Integer escalas;
    private LocalDateTime dataHoraConsulta;
    private String moeda;
    private String url;
    private String fonte;
    private Boolean promocao;
}
