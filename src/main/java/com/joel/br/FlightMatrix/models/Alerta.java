package com.joel.br.FlightMatrix.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table
@AllArgsConstructor
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "origem_codigo", nullable = false)
    private Aeroporto origem;

    @ManyToOne
    @JoinColumn(name = "destino_codigo", nullable = false)
    private Aeroporto destino;

    private LocalDate dataIdaMinima;

    private LocalDate dataIdaMaxima;

    private LocalDate dataVoltaMinima;

    private LocalDate dataVoltaMaxima;

    @Column(precision = 10, scale = 2)
    private BigDecimal precoMaximo;

    private Integer tempoMinimoPermanencia;

    private Integer tempoMaximoPermanencia;

    private Integer escalasMaximas;

    private String companhiasAereas;

    @Column(nullable = false)
    private Boolean ativo;

    private LocalDateTime ultimaNotificacao;

    @PrePersist
    protected void onCreate() {
        ativo = true;
    }
}
