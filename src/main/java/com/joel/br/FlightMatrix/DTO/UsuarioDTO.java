package com.joel.br.FlightMatrix.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "O nome do usuário é obrigatório")
    private String nome;

    @NotBlank(message = "O email do usuário é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private LocalDateTime dataCadastro;
    private Boolean ativo;
}
