package com.joel.br.FlightMatrix.models;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_precos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPreco {

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
    private String companhiaAerea;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private LocalDateTime dataHoraConsulta;

    @Column(nullable = false, length = 3)
    private String moeda;

    @ManyToOne
    @JoinColumn(name = "fonte_id", nullable = false)
    private FontePassagem fonte;
}