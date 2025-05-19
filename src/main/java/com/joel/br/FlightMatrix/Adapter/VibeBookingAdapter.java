package com.joel.br.FlightMatrix.Adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joel.br.FlightMatrix.DTO.PassagemExternaDTO;
import com.joel.br.FlightMatrix.exceptions.IntegracaoAPIException;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter para a API de Vibe Booking
 * Realiza consultas de passagens aéreas na API do Vibe Booking
 */
@Slf4j
public class VibeBookingAdapter implements FontePassagemAdapter {

    private final FontePassagem fonte;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiUrl = "https://booking-data.p.rapidapi.com/lookup";
    private final String apiKey;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public VibeBookingAdapter(FontePassagem fonte, String apiKey) {
        this.fonte = fonte;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        
        // Configuração do cliente HTTP com timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getNome() {
        return fonte.getNome();
    }

    @Override
    public List<Passagem> buscarPassagensIda(Aeroporto origem, Aeroporto destino, LocalDate dataIda) {
        try {
            log.info("Buscando passagens de ida na API Vibe Booking: {} -> {} ({})", 
                    origem.getCodigo(), destino.getCodigo(), dataIda);
            
            // Montar a requisição para a API
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
            urlBuilder.addQueryParameter("origin", origem.getCodigo());
            urlBuilder.addQueryParameter("destination", destino.getCodigo());
            urlBuilder.addQueryParameter("departureDate", dataIda.format(DATE_FORMATTER));
            
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("X-RapidAPI-Key", apiKey)
                    .addHeader("X-RapidAPI-Host", "booking-data.p.rapidapi.com")
                    .build();
            
            // Executar a chamada e processar o resultado
            return executarChamadaEConverterResultado(request, origem, destino, dataIda, null);
            
        } catch (Exception e) {
            log.error("Erro ao buscar passagens de ida na API Vibe Booking", e);
            throw new IntegracaoAPIException("Falha ao consultar API Vibe Booking: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Passagem> buscarPassagensIdaVolta(Aeroporto origem, Aeroporto destino, LocalDate dataIda, LocalDate dataVolta) {
        try {
            log.info("Buscando passagens de ida e volta na API Vibe Booking: {} -> {} ({} - {})", 
                    origem.getCodigo(), destino.getCodigo(), dataIda, dataVolta);
            
            // Montar a requisição para a API
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
            urlBuilder.addQueryParameter("origin", origem.getCodigo());
            urlBuilder.addQueryParameter("destination", destino.getCodigo());
            urlBuilder.addQueryParameter("departureDate", dataIda.format(DATE_FORMATTER));
            urlBuilder.addQueryParameter("returnDate", dataVolta.format(DATE_FORMATTER));
            
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("X-RapidAPI-Key", apiKey)
                    .addHeader("X-RapidAPI-Host", "booking-data.p.rapidapi.com")
                    .build();
            
            // Executar a chamada e processar o resultado
            return executarChamadaEConverterResultado(request, origem, destino, dataIda, dataVolta);
            
        } catch (Exception e) {
            log.error("Erro ao buscar passagens de ida e volta na API Vibe Booking", e);
            throw new IntegracaoAPIException("Falha ao consultar API Vibe Booking: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Passagem> buscarPassagensIdaFlexivel(Aeroporto origem, Aeroporto destino, LocalDate dataIdaMinima, LocalDate dataIdaMaxima) {
        // Para implementação de busca com datas flexíveis, precisamos fazer múltiplas chamadas
        // uma para cada dia no intervalo
        List<Passagem> todasPassagens = new ArrayList<>();
        
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
    public List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino, LocalDate dataIdaMinima, LocalDate dataIdaMaxima, LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        // Busca flexível de ida e volta requer múltiplas combinações de datas
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

    @Override
    public boolean isOperacional() {
        try {
            // Teste simples para verificar se a API está respondendo
            Request request = new Request.Builder()
                    .url("https://booking-data.p.rapidapi.com/status")
                    .addHeader("X-RapidAPI-Key", apiKey)
                    .addHeader("X-RapidAPI-Host", "booking-data.p.rapidapi.com")
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("Erro ao verificar status da API Vibe Booking", e);
            return false;
        }
    }
    
    @Override
    public void close() throws Exception {
        // Fecha recursos se necessário
        // O OkHttpClient não precisa ser fechado explicitamente, mas podemos 
        // fazer limpeza de outros recursos aqui se houver
    }
    
    // Método auxiliar para executar a chamada HTTP e converter o resultado
    private List<Passagem> executarChamadaEConverterResultado(Request request, Aeroporto origem, Aeroporto destino, 
                                                             LocalDate dataIda, LocalDate dataVolta) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }
            
            String responseBody = response.body().string();
            return converterResponseParaPassagens(responseBody, origem, destino, dataIda, dataVolta);
        }
    }
    
    // Método para lidar com respostas de erro
    private void handleErrorResponse(Response response) {
        int statusCode = response.code();
        String errorBody = null;
        
        try {
            errorBody = response.body().string();
        } catch (IOException e) {
            log.error("Não foi possível ler o corpo da resposta de erro", e);
        }
        
        String message = String.format("API Vibe Booking retornou erro: %d - %s", statusCode, response.message());
        log.error("{} - Body: {}", message, errorBody);
        
        if (statusCode == 429) {
            throw new IntegracaoAPIException("Limite de requisições excedido na API Vibe Booking", HttpStatus.TOO_MANY_REQUESTS);
        } else if (statusCode >= 500) {
            throw new IntegracaoAPIException("Erro interno no servidor da API Vibe Booking", HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            throw new IntegracaoAPIException(message, HttpStatus.valueOf(statusCode));
        }
    }
    
    // Converte o JSON de resposta em objetos Passagem
    private List<Passagem> converterResponseParaPassagens(String responseBody, Aeroporto origem, Aeroporto destino, 
                                                         LocalDate dataIda, LocalDate dataVolta) {
        List<Passagem> passagens = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultsNode = rootNode.path("results");
            
            if (resultsNode.isArray()) {
                for (JsonNode resultNode : resultsNode) {
                    try {
                        PassagemExternaDTO dto = objectMapper.treeToValue(resultNode, PassagemExternaDTO.class);
                        Passagem passagem = converterDTOParaPassagem(dto, origem, destino, dataIda, dataVolta);
                        passagens.add(passagem);
                    } catch (Exception e) {
                        log.warn("Erro ao converter resultado para passagem: {}", e.getMessage());
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Erro ao processar JSON da resposta", e);
            throw new IntegracaoAPIException("Erro ao processar resposta da API: " + e.getMessage(), e);
        }
        
        return passagens;
    }
    
    // Converte um DTO externo para o modelo Passagem
    private Passagem converterDTOParaPassagem(PassagemExternaDTO dto, Aeroporto origem, Aeroporto destino, 
                                             LocalDate dataIda, LocalDate dataVolta) {
        Passagem passagem = new Passagem();
        
        // Dados básicos
        passagem.setOrigem(origem);
        passagem.setDestino(destino);
        passagem.setDataIda(dataIda);
        passagem.setDataVolta(dataVolta);
        passagem.setFonte(fonte);
        
        // Companhia aérea
        if (dto.getResultados() != null && !dto.getResultados().isEmpty()) {
            PassagemExternaDTO.ResultadoVooDTO resultado = dto.getResultados().get(0);
            
            // Preço
            if (resultado.getPreco() != null) {
                passagem.setPreco(resultado.getPreco());
                passagem.setMoeda(resultado.getMoeda() != null ? resultado.getMoeda() : "USD");
            }
            
            // Escalas
            if (resultado.getQuantidadeEscalas() != null) {
                passagem.setEscalas(resultado.getQuantidadeEscalas());
            } else {
                passagem.setEscalas(0);
            }
            
            // URL de reserva
            if (resultado.getUrlReserva() != null) {
                passagem.setUrl(resultado.getUrlReserva());
            }
            
            // Companhia aérea - extrair do primeiro trecho
            if (resultado.getTrechos() != null && !resultado.getTrechos().isEmpty()) {
                passagem.setCompanhiaAerea(resultado.getTrechos().get(0).getCompanhiaAerea());
                
                // Duração
                if (resultado.getDuracao() != null && resultado.getDuracao().getTotalMinutos() != null) {
                    passagem.setDuracaoMinutos(resultado.getDuracao().getTotalMinutos());
                }
            }
        }
        
        return passagem;
    }
} 