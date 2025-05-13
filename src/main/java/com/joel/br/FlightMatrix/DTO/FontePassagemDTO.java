package com.joel.br.FlightMatrix.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FontePassagemDTO {

    private Long id;

    @NotBlank(message = "O nome da fonte é obrigatório")
    private String nome;

    @NotBlank(message = "A URL da fonte é obrigatória")
    private String url;

    @NotBlank(message = "O tipo da fonte é obrigatório")
    private String tipo;

    private Boolean ativa;

    private String configuracao;
}