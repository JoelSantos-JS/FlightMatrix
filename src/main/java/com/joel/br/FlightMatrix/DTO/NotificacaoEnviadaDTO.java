package com.joel.br.FlightMatrix.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoEnviadaDTO {

    private Long id;
    private Long alertaId;
    private Long passagemId;
    private LocalDateTime dataHoraEnvio;
    private String tipoNotificacao;
    private Boolean sucesso;
    private String conteudo;
}