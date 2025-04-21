package com.joel.br.FlightMatrix.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    private LocalDateTime dataCadastro;

    private Boolean ativo;


    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alerta> alertas = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        ativo = true;
    }

}
