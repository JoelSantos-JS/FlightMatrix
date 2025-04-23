package com.joel.br.FlightMatrix.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table
public class FontePassagem  {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;


    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Boolean ativa;

    @Column(columnDefinition = "TEXT")
    private String configuracao;
}
