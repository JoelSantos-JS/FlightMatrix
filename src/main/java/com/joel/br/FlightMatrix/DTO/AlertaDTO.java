package com.joel.br.FlightMatrix.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {

    private Long id;
    private Long usuarioId;

    @NotBlank(message = "O código do aeroporto de origem é obrigatório")
    private String origem;

    @NotBlank(message = "O código do aeroporto de destino é obrigatório")
    private String destino;

    private LocalDate dataIdaMinima;
    private LocalDate dataIdaMaxima;
    private LocalDate dataVoltaMinima;
    private LocalDate dataVoltaMaxima;
    private BigDecimal precoMaximo;
    private Integer tempoMinimoPermanencia;
    private Integer tempoMaximoPermanencia;
    private Integer escalasMaximas;
    private String companhiasAereas;
    private Boolean ativo;
    private LocalDateTime ultimaNotificacao;
}
