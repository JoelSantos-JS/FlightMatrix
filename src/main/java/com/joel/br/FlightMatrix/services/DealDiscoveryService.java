package com.joel.br.FlightMatrix.services;

import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.HistoricoPrecoRepository;
import com.joel.br.FlightMatrix.repository.PassagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável por identificar ofertas imperdíveis de passagens aéreas
 * baseado em diferentes critérios de análise.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DealDiscoveryService {

    private final PassagemRepository passagemRepository;
    private final HistoricoPrecoRepository historicoPrecoRepository;
    
    // Configurações para detecção de ofertas
    @Value("${flightmatrix.deal.percentagemQuedaPreco:20}")
    private int percentagemQuedaPreco; // Percentual de queda de preço para considerar uma oferta
    
    @Value("${flightmatrix.deal.percentagemAbaixoMedia:15}")
    private int percentagemAbaixoMedia; // Percentual abaixo da média para considerar uma oferta
    
    @Value("${flightmatrix.deal.diasHistorico:30}")
    private int diasHistorico; // Número de dias para considerar no histórico
    
    @Value("${flightmatrix.deal.limiteMinimoAereas:50}")
    private int limiteMinimoAereas; // Limite mínimo para preços de passagens aéreas domésticas
    
    @Value("${flightmatrix.deal.limiteOfertaLowCost:150}")
    private int limiteOfertaLowCost; // Limite para considerar uma passagem low-cost como oferta
    
    @Value("${flightmatrix.deal.limiteMaximoEscalas:1}")
    private int limiteMaximoEscalas; // Número máximo de escalas para um voo ser considerado boa oferta

    /**
     * Identifica as melhores ofertas de passagens a partir de uma lista de passagens disponíveis.
     * 
     * @param passagens Lista de passagens a serem analisadas
     * @return Lista das melhores ofertas encontradas
     */
    public List<Passagem> identificarMelhoresOfertas(List<Passagem> passagens) {
        log.info("Iniciando análise para identificar melhores ofertas entre {} passagens", passagens.size());
        
        List<Passagem> ofertasIdentificadas = new ArrayList<>();
        
        // Filtra apenas passagens com preço válido e não nulo
        List<Passagem> passagensValidas = passagens.stream()
                .filter(p -> p.getPreco() != null && p.getPreco().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        for (Passagem passagem : passagensValidas) {
            if (isOfertaImperdivel(passagem)) {
                ofertasIdentificadas.add(passagem);
            }
        }
        
        // Ordena as ofertas por melhor valor (menor preço primeiro)
        ofertasIdentificadas.sort(Comparator.comparing(Passagem::getPreco));
        
        log.info("Identificadas {} ofertas imperdíveis", ofertasIdentificadas.size());
        return ofertasIdentificadas;
    }

    /**
     * Determina se uma passagem é considerada uma oferta imperdível.
     * Avalia diversos critérios como queda de preço, comparação com histórico e preço absoluto.
     * 
     * @param passagem Passagem a ser avaliada
     * @return true se a passagem for considerada uma oferta imperdível, false caso contrário
     */
    public boolean isOfertaImperdivel(Passagem passagem) {
        // Critério 1: Queda significativa em relação ao preço anterior (se disponível)
        if (isQuedaSignificativaPreco(passagem)) {
            log.debug("Passagem {} -> {} ({}): queda significativa de preço detectada", 
                    passagem.getOrigem().getCodigo(), passagem.getDestino().getCodigo(), passagem.getDataIda());
            return true;
        }
        
        // Critério 2: Preço muito abaixo da média histórica
        if (isPrecoAbaixoMediaHistorica(passagem)) {
            log.debug("Passagem {} -> {} ({}): preço abaixo da média histórica", 
                    passagem.getOrigem().getCodigo(), passagem.getDestino().getCodigo(), passagem.getDataIda());
            return true;
        }
        
        // Critério 3: Preço absoluto muito baixo (baseado em tipo de voo)
        if (isPrecoAbsolutoMuitoBaixo(passagem)) {
            log.debug("Passagem {} -> {} ({}): preço absoluto muito baixo", 
                    passagem.getOrigem().getCodigo(), passagem.getDestino().getCodigo(), passagem.getDataIda());
            return true;
        }
        
        // Critério 4: Combinação de fatores (baixo número de escalas + preço razoável)
        if (isCombinaçãoPositivaFatores(passagem)) {
            log.debug("Passagem {} -> {} ({}): combinação positiva de fatores", 
                    passagem.getOrigem().getCodigo(), passagem.getDestino().getCodigo(), passagem.getDataIda());
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se a passagem teve uma queda significativa em relação ao seu preço anterior.
     */
    private boolean isQuedaSignificativaPreco(Passagem passagem) {
        if (passagem.getPrecoAnterior() == null || passagem.getPrecoAnterior().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Calcula percentual de queda
        BigDecimal precoAtual = passagem.getPreco();
        BigDecimal precoAnterior = passagem.getPrecoAnterior();
        
        BigDecimal percentualQueda = BigDecimal.ONE.subtract(
                precoAtual.divide(precoAnterior, 4, RoundingMode.HALF_UP)
        ).multiply(BigDecimal.valueOf(100));
        
        // Verifica se a queda é significativa (acima do limiar configurado)
        return percentualQueda.compareTo(BigDecimal.valueOf(percentagemQuedaPreco)) >= 0;
    }
    
    /**
     * Verifica se o preço da passagem está significativamente abaixo da média histórica.
     */
    private boolean isPrecoAbaixoMediaHistorica(Passagem passagem) {
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(diasHistorico);
        
        // Busca histórico de preços para a mesma rota
        var historicos = historicoPrecoRepository.findHistoricoPeriodicoByRota(
                passagem.getOrigem(), 
                passagem.getDestino(),
                dataInicio, 
                LocalDateTime.now()
        );
        
        if (historicos.isEmpty()) {
            return false; // Sem histórico para comparar
        }
        
        // Calcula preço médio histórico
        BigDecimal somaPrecos = historicos.stream()
                .map(h -> h.getPreco())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal precoMedio = somaPrecos.divide(
                BigDecimal.valueOf(historicos.size()), 
                2, 
                RoundingMode.HALF_UP
        );
        
        // Calcula limite para oferta (preço médio - percentual configurado)
        BigDecimal limiteOferta = precoMedio.multiply(
                BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(percentagemAbaixoMedia).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                )
        );
        
        return passagem.getPreco().compareTo(limiteOferta) <= 0;
    }
    
    /**
     * Verifica se o preço absoluto da passagem é muito baixo para o tipo de rota.
     * Os critérios variam de acordo com a distância e tipo de voo.
     */
    private boolean isPrecoAbsolutoMuitoBaixo(Passagem passagem) {
        // Determina tipo de rota (doméstica ou internacional simplificado)
        String paisOrigem = passagem.getOrigem().getCodigo().substring(0, 2);
        String paisDestino = passagem.getDestino().getCodigo().substring(0, 2);
        boolean rotaDomestica = paisOrigem.equals(paisDestino);
        
        if (rotaDomestica) {
            // Considera a distância implícita na análise de domésticas
            return passagem.getPreco().compareTo(BigDecimal.valueOf(limiteMinimoAereas)) <= 0;
        } else {
            // Para internacional, varia de acordo com a companhia e tipo de voo
            if (isLowCostCarrier(passagem.getCompanhiaAerea())) {
                return passagem.getPreco().compareTo(BigDecimal.valueOf(limiteOfertaLowCost)) <= 0;
            } else {
                // Para outras companhias, o limiar seria maior, mas ainda consideramos baixo custo
                return passagem.getPreco().compareTo(BigDecimal.valueOf(300)) <= 0;
            }
        }
    }
    
    /**
     * Verifica se a passagem apresenta uma combinação positiva de fatores,
     * como preço razoável com poucas escalas ou viagem de longa distância com preço acessível.
     */
    private boolean isCombinaçãoPositivaFatores(Passagem passagem) {
        // Voos diretos ou com poucas escalas a preço razoável
        if (passagem.getEscalas() != null && 
            passagem.getEscalas() <= limiteMaximoEscalas && 
            isPrecoRazoavel(passagem)) {
            return true;
        }
        
        // Viagem com data próxima (last minute) e preço bom
        if (isLastMinute(passagem) && isPrecoRazoavel(passagem)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se a companhia aérea é considerada low-cost.
     */
    private boolean isLowCostCarrier(String companhiaAerea) {
        if (companhiaAerea == null) return false;
        
        // Lista de companhias aéreas low-cost conhecidas
        List<String> lowCostCarriers = List.of(
                "RYANAIR", "EASYJET", "WIZZ", "VUELING", "LEVEL", 
                "TRANSAVIA", "AZUL", "GOL", "JETBLUE", "SOUTHWEST", 
                "FRONTIER", "SPIRIT", "FLYBONDI", "JETSMART"
        );
        
        return lowCostCarriers.stream()
                .anyMatch(c -> companhiaAerea.toUpperCase().contains(c));
    }
    
    /**
     * Verifica se o preço está em uma faixa razoável para o tipo de voo.
     * Esta é uma análise mais flexível que o preço absoluto muito baixo.
     */
    private boolean isPrecoRazoavel(Passagem passagem) {
        // Determina tipo de rota
        String paisOrigem = passagem.getOrigem().getCodigo().substring(0, 2);
        String paisDestino = passagem.getDestino().getCodigo().substring(0, 2);
        boolean rotaDomestica = paisOrigem.equals(paisDestino);
        
        if (rotaDomestica) {
            return passagem.getPreco().compareTo(BigDecimal.valueOf(200)) <= 0;
        } else {
            // Lógica para internacional
            return passagem.getPreco().compareTo(BigDecimal.valueOf(600)) <= 0;
        }
    }
    
    /**
     * Verifica se a passagem é para uma viagem próxima (last minute).
     */
    private boolean isLastMinute(Passagem passagem) {
        LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
        long diasAtePartida = ChronoUnit.DAYS.between(hoje, passagem.getDataIda());
        
        // Considera last minute se for menos de 7 dias para a partida
        return diasAtePartida >= 0 && diasAtePartida <= 7;
    }
    
    /**
     * Busca as melhores ofertas disponíveis no sistema para uma origem e destino específicos.
     * 
     * @param codigoOrigem Código IATA do aeroporto de origem
     * @param codigoDestino Código IATA do aeroporto de destino
     * @param dataInicio Data inicial para a busca
     * @param dataFim Data final para a busca
     * @return Lista das melhores ofertas encontradas
     */
    public List<Passagem> buscarMelhoresOfertas(String codigoOrigem, String codigoDestino, 
                                                LocalDate dataInicio, LocalDate dataFim) {
        
        log.info("Buscando melhores ofertas para rota {} -> {} entre {} e {}", 
                codigoOrigem, codigoDestino, dataInicio, dataFim);
        
        // Utiliza o repositório para buscar passagens disponíveis
        List<Passagem> passagensDisponiveis = passagemRepository
                .findByOrigemCodigoAndDestinoCodigoAndDataIdaBetween(
                        codigoOrigem, codigoDestino, dataInicio, dataFim);
        
        return identificarMelhoresOfertas(passagensDisponiveis);
    }
} 