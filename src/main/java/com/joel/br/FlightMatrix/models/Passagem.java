package com.joel.br.FlightMatrix.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "origem_codigo", nullable = false)
    private Aeroporto origem;

    @ManyToOne
    @JoinColumn(name = "destino_codigo", nullable = false)
    private Aeroporto destino;

    @Column(nullable = false)
    private LocalDate dataIda;

    private LocalDate dataVolta;

    private BigDecimal precoAnterior;

    private String companhiaAerea;

    @Column(length = 500)
    private String logoUrl;

    private Integer escalas;

    private Integer duracaoMinutos;

    private LocalDateTime dataHoraConsulta;

    private String moeda;

    private String moedaOriginal;

    private BigDecimal precoOriginal;

    private String url;

    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "fonte_id", nullable = false)
    private FontePassagem fonte;

    @PrePersist
    protected void onCreate() {
        dataHoraConsulta = LocalDateTime.now();
        if (moeda == null) {
            moeda = "BRL";
        }
    }
}
