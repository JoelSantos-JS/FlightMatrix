package com.joel.br.FlightMatrix.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AeroportoDTO {

    @NotBlank(message = "O código do aeroporto é obrigatório")
    @Size(min = 3, max = 3, message = "O código do aeroporto deve ter exatamente 3 caracteres")
    @Pattern(regexp = "[A-Z]{3}", message = "O código do aeroporto deve conter apenas letras maiúsculas")
    private String codigo;

    @NotBlank(message = "O nome do aeroporto é obrigatório")
    private String nome;

    @NotBlank(message = "A cidade do aeroporto é obrigatória")
    private String cidade;

    @NotBlank(message = "O país do aeroporto é obrigatório")
    private String pais;
}
