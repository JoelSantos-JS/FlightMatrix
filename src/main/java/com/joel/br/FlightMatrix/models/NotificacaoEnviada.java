package com.joel.br.FlightMatrix.models;

import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.Passagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_enviadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoEnviada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alerta_id", nullable = false)
    private Alerta alerta;

    @ManyToOne
    @JoinColumn(name = "passagem_id", nullable = false)
    private Passagem passagem;

    @Column(nullable = false)
    private LocalDateTime dataHoraEnvio;

    @Column(nullable = false)
    private String tipoNotificacao;

    @Column(nullable = false)
    private Boolean sucesso;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @PrePersist
    protected void onCreate() {
        dataHoraEnvio = LocalDateTime.now();
    }
}