package com.joel.br.FlightMatrix.Adapter;

import com.joel.br.FlightMatrix.DTO.PassagemExternaDTO;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.util.AirlineUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BookingDataAdapter implements FontePassagemAdapter {

    private final FontePassagem fonte;
    private final String apiKey;
    private final AsyncHttpClient client;
    private final ObjectMapper objectMapper;
    
    private static final String API_HOST = "booking-data.p.rapidapi.com";
    private static final String BASE_URL = "https://booking-data.p.rapidapi.com/booking-app/flights";
    
    public BookingDataAdapter(FontePassagem fonte, String apiKey) {
        this.fonte = fonte;
        this.apiKey = apiKey;
        this.client = new DefaultAsyncHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getNome() {
        return "BookingData";
    }
    
    @Override
    public boolean isOperacional() {
        try {
            // Teste simples para verificar se a API está operacional
            String testUrl = BASE_URL + "/airports?query=NYC";
            Response response = client.prepare("GET", testUrl)
                    .setHeader("x-rapidapi-key", apiKey)
                    .setHeader("x-rapidapi-host", API_HOST)
                    .execute()
                    .toCompletableFuture()
                    .get(5, TimeUnit.SECONDS);
                    
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            log.error("Erro ao verificar se a API BookingData está operacional", e);
            return false;
        }
    }
    
    @Override
    public List<Passagem> buscarPassagensIda(Aeroporto origem, Aeroporto destino, LocalDate dataIda) {
        String url = BASE_URL + "/search-one-way" +
                "?fromId=" + origem.getCodigo() +
                "&toId=" + destino.getCodigo() +
                "&departureDate=" + dataIda.toString() +
                "&cabinClass=ECONOMY" +
                "&numberOfStops=all";
                
        return buscarPassagens(url, origem, destino, dataIda, null);
    }
    
    @Override
    public List<Passagem> buscarPassagensIdaVolta(Aeroporto origem, Aeroporto destino, 
                                                 LocalDate dataIda, LocalDate dataVolta) {
        String url = BASE_URL + "/search-return" +
                "?fromId=" + origem.getCodigo() +
                "&toId=" + destino.getCodigo() +
                "&departureDate=" + dataIda.toString() +
                "&returnDate=" + dataVolta.toString() +
                "&cabinClass=ECONOMY" +
                "&numberOfStops=all";
                
        return buscarPassagens(url, origem, destino, dataIda, dataVolta);
    }

    @Override
    public List<Passagem> buscarPassagensIdaFlexivel(Aeroporto origem, Aeroporto destino, 
                                                  LocalDate dataIdaMinima, LocalDate dataIdaMaxima) {
        List<Passagem> todasPassagens = new ArrayList<>();
        
        // Para implementação de busca com datas flexíveis, precisamos fazer múltiplas chamadas
        // uma para cada dia no intervalo
        LocalDate dataAtual = dataIdaMinima;
        while (!dataAtual.isAfter(dataIdaMaxima)) {
            try {
                List<Passagem> passagensData = buscarPassagensIda(origem, destino, dataAtual);
                todasPassagens.addAll(passagensData);
            } catch (Exception e) {
                log.warn("Erro ao buscar passagens para a data {}: {}", dataAtual, e.getMessage());
                // Continuamos a busca para outras datas mesmo com erro em uma data específica
            }
            dataAtual = dataAtual.plusDays(1);
        }
        
        return todasPassagens;
    }
    
    @Override
    public List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino, 
                                                   LocalDate dataIdaMinima, LocalDate dataIdaMaxima, 
                                                   LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        List<Passagem> todasPassagens = new ArrayList<>();
        
        // Defina um limite razoável de combinações para evitar sobrecarga
        final int LIMITE_COMBINACOES = 30;
        int contadorCombinacoes = 0;
        
        LocalDate dataIdaAtual = dataIdaMinima;
        while (!dataIdaAtual.isAfter(dataIdaMaxima) && contadorCombinacoes < LIMITE_COMBINACOES) {
            
            LocalDate dataVoltaAtual = dataVoltaMinima;
            // Apenas considere datas de volta posteriores à data de ida
            if (dataVoltaAtual.isBefore(dataIdaAtual)) {
                dataVoltaAtual = dataIdaAtual.plusDays(1);
            }
            
            while (!dataVoltaAtual.isAfter(dataVoltaMaxima) && contadorCombinacoes < LIMITE_COMBINACOES) {
                try {
                    List<Passagem> passagensCombinacao = buscarPassagensIdaVolta(origem, destino, dataIdaAtual, dataVoltaAtual);
                    todasPassagens.addAll(passagensCombinacao);
                } catch (Exception e) {
                    log.warn("Erro ao buscar passagens para a combinação {} - {}: {}", 
                            dataIdaAtual, dataVoltaAtual, e.getMessage());
                }
                dataVoltaAtual = dataVoltaAtual.plusDays(1);
                contadorCombinacoes++;
            }
            
            dataIdaAtual = dataIdaAtual.plusDays(1);
        }
        
        return todasPassagens;
    }
    
    private List<Passagem> buscarPassagens(String url, Aeroporto origem, Aeroporto destino, 
                                          LocalDate dataIda, LocalDate dataVolta) {
        List<Passagem> passagens = new ArrayList<>();
        
        try {
            log.info("Buscando passagens na API BookingData: {} -> {}, ida: {}, volta: {}", 
                    origem.getCodigo(), destino.getCodigo(), dataIda, dataVolta);
            
            CompletableFuture<Response> future = client.prepare("GET", url)
                    .setHeader("x-rapidapi-key", apiKey)
                    .setHeader("x-rapidapi-host", API_HOST)
                    .execute()
                    .toCompletableFuture();
                
            Response response = future.get(10, TimeUnit.SECONDS);
            
            if (response.getStatusCode() == 200) {
                String responseBody = response.getResponseBody();
                
                // Convertendo JSON para DTOs
                PassagemExternaDTO resultado = objectMapper.readValue(responseBody, PassagemExternaDTO.class);
                
                // Convertendo DTO para modelo de domínio
                passagens = converterParaPassagens(resultado, origem, destino, dataIda, dataVolta);
                
                log.info("Encontradas {} passagens na API BookingData", passagens.size());
            } else {
                log.error("Erro ao buscar passagens na API BookingData: {} - {}", 
                         response.getStatusCode(), response.getStatusText());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar passagens na API BookingData", e);
        }
        
        return passagens;
    }
    
    private List<Passagem> converterParaPassagens(PassagemExternaDTO dto, 
                                                 Aeroporto origem, Aeroporto destino,
                                                 LocalDate dataIda, LocalDate dataVolta) {
        List<Passagem> passagens = new ArrayList<>();
        
        if (dto.getResultados() != null) {
            dto.getResultados().forEach(resultado -> {
                try {
                    Passagem passagem = new Passagem();
                    passagem.setOrigem(origem);
                    passagem.setDestino(destino);
                    passagem.setDataIda(dataIda);
                    passagem.setDataVolta(dataVolta);
                    
                    // Configurar companhia aérea
                    if (resultado.getTrechos() != null && !resultado.getTrechos().isEmpty()) {
                        String companhiaAerea = resultado.getTrechos().get(0).getCompanhiaAerea();
                        passagem.setCompanhiaAerea(companhiaAerea);
                        // Adiciona URL do logo baseado no código da companhia
                        passagem.setLogoUrl(AirlineUtil.getAirlineLogo(companhiaAerea));
                    }
                    
                    // Configurar preço com conversão se necessário
                    if (resultado.getPreco() != null) {
                        String moeda = resultado.getMoeda() != null ? resultado.getMoeda() : "USD";
                        BigDecimal precoOriginal = resultado.getPreco();
                        
                        // Converte para BRL se o preço estiver em USD
                        if ("USD".equalsIgnoreCase(moeda)) {
                            passagem.setPreco(AirlineUtil.convertCurrency(precoOriginal, moeda));
                            passagem.setMoeda("BRL");
                            // Armazena o preço original antes da conversão
                            passagem.setPrecoOriginal(precoOriginal);
                            passagem.setMoedaOriginal(moeda);
                        } else {
                            passagem.setPreco(precoOriginal);
                            passagem.setMoeda(moeda);
                        }
                    }
                    
                    // Configurar escalas
                    passagem.setEscalas(resultado.getQuantidadeEscalas());
                    
                    // Configurar URL e fonte
                    passagem.setUrl(resultado.getUrlReserva());
                    passagem.setFonte(fonte);
                    
                    // Configurar timestamp da consulta
                    passagem.setDataHoraConsulta(LocalDateTime.now());
                    
                    // Calcular duração total em minutos
                    if (resultado.getDuracao() != null && resultado.getDuracao().getTotalMinutos() != null) {
                        passagem.setDuracaoMinutos(resultado.getDuracao().getTotalMinutos());
                    }
                    
                    passagens.add(passagem);
                } catch (Exception e) {
                    log.error("Erro ao converter passagem da API BookingData", e);
                }
            });
        }
        
        return passagens;
    }
    
    @Override
    public void close() throws Exception {
        if (client != null && !client.isClosed()) {
            client.close();
        }
    }
} 