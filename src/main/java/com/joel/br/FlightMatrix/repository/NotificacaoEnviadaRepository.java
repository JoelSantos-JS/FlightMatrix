package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.NotificacaoEnviada;
import com.joel.br.FlightMatrix.models.Passagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoEnviadaRepository  extends JpaRepository<NotificacaoEnviada, Long> {

    List<NotificacaoEnviada> findByAlerta(Alerta alerta);

    List<NotificacaoEnviada> findByAlertaAndDataHoraEnvioAfter(
            Alerta alerta,
            LocalDateTime dataHora
    );

    Optional<NotificacaoEnviada> findByAlertaAndPassagem(Alerta alerta, Passagem passagem);

    List<NotificacaoEnviada> findByTipoNotificacao(String tipoNotificacao);
}
