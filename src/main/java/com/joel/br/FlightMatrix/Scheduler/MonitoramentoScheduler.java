package com.joel.br.FlightMatrix.Scheduler;

import com.joel.br.FlightMatrix.Adapter.FontePassagemAdapter;
import com.joel.br.FlightMatrix.Factory.VibeBookingAdapterFactory;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.repository.FontePassagemRepository;
import com.joel.br.FlightMatrix.repository.PassagemRepository;
import com.joel.br.FlightMatrix.services.AlertaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Scheduler para monitoramento periódico de preços de passagens aéreas
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoramentoScheduler {

    private final AeroportoRepository aeroportoRepository;
    private final FontePassagemRepository fontePassagemRepository;
    private final PassagemRepository passagemRepository;
    private final VibeBookingAdapterFactory vibeBookingAdapterFactory;
    private final AlertaService alertaService;
    
    @Value("${flightmatrix.monitoramento.paralelo:true}")
    private boolean executarEmParalelo;
    
    @Value("${flightmatrix.monitoramento.timeoutMinutos:10}")
    private long timeoutMinutos;
    
    @Value("${flightmatrix.monitoramento.diasMonitoramento:60}")
    private int diasMonitoramento;
    
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Executa o monitoramento periódico de rotas populares
     * Configurado para executar diariamente à 01:00 AM
     */

    private List<Map.Entry<Aeroporto, Aeroporto>> gerarCombinacoes(List<Aeroporto> aeroportos) {
        List<Map.Entry<Aeroporto, Aeroporto>> combinacoes = new ArrayList<>();
        
        for (int i = 0; i < aeroportos.size(); i++) {
            for (int j = 0; j < aeroportos.size(); j++) {
                if (i != j) { // Evita origem igual ao destino
                    combinacoes.add(new AbstractMap.SimpleEntry<>(aeroportos.get(i), aeroportos.get(j)));
                }
            }
        }
        
        return combinacoes;
    }
    
    /**
     * Executa monitoramento de forma sequencial
     */

    
    /**
     * Executa monitoramento de forma paralela para melhor performance
     */

    
    /**
     * Busca passagens para uma fonte/origem/destino/período específico
     */

    
    /**
     * Seleciona algumas datas dentro do período para monitoramento
     * (para não fazer buscas para todos os dias do período)
     */
    private List<LocalDate> selecionarDatasParaMonitoramento(LocalDate dataInicio, LocalDate dataFim) {
        long diasTotais = ChronoUnit.DAYS.between(dataInicio, dataFim);
        
        // Se forem menos de 10 dias, verifica todos os dias
        if (diasTotais <= 10) {
            return dataInicio.datesUntil(dataFim.plusDays(1)).collect(Collectors.toList());
        }
        
        // Caso contrário, seleciona algumas datas estratégicas:
        // - Próximos 3 dias 
        // - A cada 5 dias no primeiro mês
        // - A cada 10 dias no resto do período
        
        List<LocalDate> datasSelecionadas = new ArrayList<>();
        
        // Próximos 3 dias
        LocalDate atual = dataInicio;
        for (int i = 0; i < 3 && atual.isBefore(dataFim); i++) {
            datasSelecionadas.add(atual);
            atual = atual.plusDays(1);
        }
        
        // A cada 5 dias no primeiro mês
        atual = dataInicio.plusDays(5);
        LocalDate umMesDepois = dataInicio.plusMonths(1);
        while (atual.isBefore(umMesDepois) && atual.isBefore(dataFim)) {
            datasSelecionadas.add(atual);
            atual = atual.plusDays(5);
        }
        
        // A cada 10 dias no resto do período
        atual = umMesDepois;
        while (atual.isBefore(dataFim)) {
            datasSelecionadas.add(atual);
            atual = atual.plusDays(10);
        }
        
        // Adiciona a data final se não foi incluída
        if (!datasSelecionadas.contains(dataFim) && !dataFim.isBefore(dataInicio)) {
            datasSelecionadas.add(dataFim);
        }
        
        return datasSelecionadas.stream().distinct().sorted().collect(Collectors.toList());
    }
}
