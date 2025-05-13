package com.joel.br.FlightMatrix.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoDiarioDTO {

    private Long usuarioId;
    private LocalDate data;
    private Integer totalPromocoes;
    private Map<String, List<PassagemDTO>> promocoesPorRota;
}