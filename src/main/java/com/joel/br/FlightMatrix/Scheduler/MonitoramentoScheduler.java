package com.joel.br.FlightMatrix.Scheduler;

import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.services.AlertaService;
import com.joel.br.FlightMatrix.services.NotificacaoService;
import com.joel.br.FlightMatrix.services.PassagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoramentoScheduler {

    private final AlertaService alertaService;
    private final PassagemService passagemService;
    private final NotificacaoService notificacaoService;


    @Scheduled(cron = "${app.scheduler.monitoramento.cron}")
    public void executarMonitoramentoPeriodico(){


        List<Alerta> alertas = alertaService.buscarAlertasParaVereficar();

        if(alertas.isEmpty()) {
            return;
        }

        Map<String, List<Alerta>> alertasPorRotas = alertas.stream()
                .collect(Collectors.groupingBy(a -> a.getOrigem().getCodigo() + "-" + a.getDestino().getCodigo() ));

        log.info("Monitorando {} rotas diferentes", alertasPorRotas.size());


        alertasPorRotas.forEach((rota, alertasRota) -> {
            String[] rotaSplit = rota.split("-");
            String origem = rotaSplit[0];
            String destino = rotaSplit[1];

            monitorarRotaAsync(origem, destino, alertasRota);
        });

    }

    @Scheduled
    public void executarMonitoramentoOfertasRelampagos() {



        List<Alerta>  alertas = alertaService.buscarAlertasParaVereficar();


        LocalDate hoje = LocalDate.now();

        LocalDate proximaSemana = LocalDate.now().plusDays(7);



        List<Alerta> alertasProximos = alertas.stream().
                filter(a -> a.getDataIdaMinima() != null && !a.getDataIdaMinima().isAfter(proximaSemana) && (a.getDataIdaMaxima() == null || !a.getDataIdaMaxima().isBefore(hoje)))
                .collect(Collectors.toList());


        if(alertasProximos.isEmpty()) {
            return;
        }

        Map<String, List<Alerta>> alertasPorRota = alertasProximos.stream()
                .collect(Collectors.groupingBy(a ->
                        a.getOrigem().getCodigo() + "-" + a.getDestino().getCodigo()));

        // Para cada rota, faz o monitoramento com prioridade
        alertasPorRota.forEach((rota, alertasRota) -> {
            String[] rotaSplit = rota.split("-");
            String origem = rotaSplit[0];
            String destino = rotaSplit[1];

            monitorarRotaAsync(origem, destino, alertasRota);
        });
    }

    @Async
    public CompletableFuture<Integer> monitorarRotaAsync(String origem, String destino, List<Alerta> alertas) {
        log.info("Monitorando rota: {} -> {}", origem, destino);


        LocalDate dataIdaMinima = alertas.stream()
                .map(Alerta::getDataIdaMinima)
                .filter(d -> d != null)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate dataIdaMaxima = alertas.stream().map(Alerta::getDataIdaMaxima)
                .filter(d -> d != null)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now().plusMonths(3));


        LocalDate dataVoltaMinima = alertas.stream().map(Alerta::getDataVoltaMinima)
                .filter(d -> d != null)
                .min(LocalDate::compareTo)
                .orElse(null);


        LocalDate dataVoltaMaxima = alertas.stream().map(Alerta::getDataVoltaMaxima)
                .filter(d -> d !=null)
                .max(LocalDate::compareTo)
                .orElse(null);

        List<Passagem> passagens = passagemService.buscasPassagensFlexiveis(
                origem, destino, dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);


        int notificacoesEnviadas = 0;

        for (Passagem passagem : passagens) {
            boolean isPromocao = passagemService.isPromocao(passagem);

            if (isPromocao) {
                int enviados = notificacaoService.processarPromocaoENotificar(passagem, true);
                notificacoesEnviadas += enviados;
            }
        }

        log.info("Enviadas {} notificações para a rota {} -> {}",
                notificacoesEnviadas, origem, destino);

        return CompletableFuture.completedFuture(notificacoesEnviadas);
    }
}
