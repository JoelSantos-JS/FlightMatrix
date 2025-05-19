package com.joel.br.FlightMatrix.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Controller para realizar consultas diretas à API Booking Data
 */
@Controller
@RequestMapping("/direct")
@RequiredArgsConstructor
@Slf4j
public class DirectAPIController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.bookingdata.api-key:48d25ca291msh89bb70a6820e66ap1c495cjsnc5b6a92e85b7}")
    private String apiKey;
    
    @Value("${app.bookingdata.api-host:booking-data.p.rapidapi.com}")
    private String apiHost;
    
    // Taxa de conversão USD para BRL (valor fixo para exemplo)
    private static final double USD_TO_BRL_RATE = 5.1;
    
    // Mapa de códigos de companhias aéreas para URLs de logos
    private static final Map<String, String> AIRLINE_LOGOS = Map.of(
        "AD", "https://logos.skyscnr.com/images/airlines/favicon/AD.png",
        "AA", "https://logos.skyscnr.com/images/airlines/favicon/AA.png",
        "LA", "https://logos.skyscnr.com/images/airlines/favicon/LA.png",
        "G3", "https://logos.skyscnr.com/images/airlines/favicon/G3.png",
        "UA", "https://logos.skyscnr.com/images/airlines/favicon/UA.png",
        "AF", "https://logos.skyscnr.com/images/airlines/favicon/AF.png",
        "BA", "https://logos.skyscnr.com/images/airlines/favicon/BA.png",
        "DL", "https://logos.skyscnr.com/images/airlines/favicon/DL.png",
        "IB", "https://logos.skyscnr.com/images/airlines/favicon/IB.png",
        "LH", "https://logos.skyscnr.com/images/airlines/favicon/LH.png"
    );
    
    // URL padrão para logos não encontrados
    private static final String DEFAULT_LOGO = "https://logos.skyscnr.com/images/airlines/favicon/airline.png";

    /**
     * Página inicial para busca direta
     */
    @GetMapping("/")
    public String home(Model model) {
        // Adiciona datas padrão
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("amanha", LocalDate.now().plusDays(1));
        model.addAttribute("umMesDepois", LocalDate.now().plusMonths(1));
        
        return "direct/index";
    }
    
    /**
     * Realiza busca direta na API de voos de ida e volta
     */
    @GetMapping("/busca-volta")
    public String buscarVoosIdaVolta(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIda,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVolta,
            @RequestParam(defaultValue = "ECONOMY") String cabinClass,
            Model model) {
        
        log.info("Busca direta com volta: {} -> {}, ida: {}, volta: {}", origem, destino, dataIda, dataVolta);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String departureDate = dataIda.format(formatter);
        String returnDate = dataVolta.format(formatter);
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            CompletableFuture<Response> future = client.prepare("GET", 
                    "https://booking-data.p.rapidapi.com/booking-app/flights/search-return" +
                    "?fromId=" + origem + 
                    "&toId=" + destino + 
                    "&departureDate=" + departureDate + 
                    "&returnDate=" + returnDate + 
                    "&cabinClass=" + cabinClass +
                    "&numberOfStops=all")
                .setHeader("x-rapidapi-key", apiKey)
                .setHeader("x-rapidapi-host", apiHost)
                .execute()
                .toCompletableFuture();
            
            Response response = future.get();
            
            if (response.getStatusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.getResponseBody());
                JsonNode dataNode = rootNode.path("data");
                
                if (dataNode.has("flights") && dataNode.path("flights").isArray()) {
                    for (JsonNode flightOption : dataNode.path("flights")) {
                        Map<String, Object> resultado = new HashMap<>();

                        // Extrai preço principal e link de um dos "travelerPrices" (geralmente o primeiro)
                        // A API retorna o preço em centavos, então dividimos por 100.
                        double price = flightOption.at("/travelerPrices/0/price/price/value").asDouble(0) / 100.0;
                        String currency = flightOption.at("/travelerPrices/0/price/price/currency/code").asText("USD");
                        String deepLink = flightOption.at("/shareableUrl").asText("");

                        // Converte para BRL se o preço estiver em USD
                        if ("USD".equals(currency)) {
                            price = price * USD_TO_BRL_RATE;
                            currency = "BRL";
                        }
                        
                        // Formata o preço com duas casas decimais
                        price = Math.round(price * 100.0) / 100.0;

                        resultado.put("preco", price);
                        resultado.put("moeda", currency);
                        resultado.put("deep_link", deepLink);

                        JsonNode bounds = flightOption.path("bounds");
                        if (bounds.isArray() && bounds.size() >= 1) {
                            // Trecho de Ida
                            JsonNode outboundBound = bounds.get(0);
                            extractLegDetails(outboundBound.path("segments"), resultado, "Ida");
                        }

                        if (bounds.isArray() && bounds.size() >= 2) {
                            // Trecho de Volta
                            JsonNode returnBound = bounds.get(1);
                            extractLegDetails(returnBound.path("segments"), resultado, "Volta");
                        }
                        resultados.add(resultado);
                    }
                } else {
                    log.warn("Nenhum voo encontrado na resposta da API (data/flights ausente ou não é array): {}", response.getResponseBody());
                }
            } else {
                log.error("Erro na chamada à API: {} - {}", response.getStatusCode(), response.getResponseBody());
            }
        } catch (Exception e) {
            log.error("Erro ao chamar ou processar a API de voos de ida e volta", e);
        }
        
        // Adiciona resultados e metadados ao modelo
        model.addAttribute("resultados", resultados);
        model.addAttribute("origem", origem);
        model.addAttribute("destino", destino);
        model.addAttribute("dataIda", dataIda);
        model.addAttribute("dataVolta", dataVolta);
        model.addAttribute("totalResultados", resultados.size());
        model.addAttribute("responseJson", ""); // Para debug, poderíamos adicionar o JSON original
        
        return "direct/resultados";
    }
    
    /**
     * Realiza busca direta na API de voos somente ida
     */
    @GetMapping("/busca-ida")
    public String buscarVoosSomenteIda(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIda,
            @RequestParam(defaultValue = "ECONOMY") String cabinClass,
            Model model) {
        
        log.info("Busca direta somente ida: {} -> {}, data: {}", origem, destino, dataIda);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String departureDate = dataIda.format(formatter);
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            CompletableFuture<Response> future = client.prepare("GET", 
                    "https://booking-data.p.rapidapi.com/booking-app/flights/search-one-way" +
                    "?fromId=" + origem + 
                    "&toId=" + destino + 
                    "&departureDate=" + departureDate + 
                    "&cabinClass=" + cabinClass +
                    "&numberOfStops=all")
                .setHeader("x-rapidapi-key", apiKey)
                .setHeader("x-rapidapi-host", apiHost)
                .execute()
                .toCompletableFuture();
            
            Response response = future.get();
            
            if (response.getStatusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.getResponseBody());
                JsonNode dataNode = rootNode.path("data");
                
                if (dataNode.has("flights") && dataNode.path("flights").isArray()) {
                    for (JsonNode flightOption : dataNode.path("flights")) {
                        Map<String, Object> resultado = new HashMap<>();

                        double price = flightOption.at("/travelerPrices/0/price/price/value").asDouble(0) / 100.0;
                        String currency = flightOption.at("/travelerPrices/0/price/price/currency/code").asText("USD");
                        String deepLink = flightOption.at("/shareableUrl").asText("");

                        // Converte para BRL se o preço estiver em USD
                        if ("USD".equals(currency)) {
                            price = price * USD_TO_BRL_RATE;
                            currency = "BRL";
                        }
                        
                        // Formata o preço com duas casas decimais
                        price = Math.round(price * 100.0) / 100.0;

                        resultado.put("preco", price);
                        resultado.put("moeda", currency);
                        resultado.put("deep_link", deepLink);

                        JsonNode bounds = flightOption.path("bounds");
                        if (bounds.isArray() && bounds.size() >= 1) {
                            // Trecho de Ida (único trecho para one-way)
                            JsonNode outboundBound = bounds.get(0);
                            extractLegDetails(outboundBound.path("segments"), resultado, ""); // Sufixo vazio para one-way
                        }
                        resultados.add(resultado);
                    }
                } else {
                    log.warn("Nenhum voo encontrado na resposta da API (data/flights ausente ou não é array): {}", response.getResponseBody());
                }
            } else {
                log.error("Erro na chamada à API: {} - {}", response.getStatusCode(), response.getResponseBody());
            }
        } catch (Exception e) {
            log.error("Erro ao chamar ou processar a API de voos somente ida", e);
        }
        
        // Adiciona resultados e metadados ao modelo
        model.addAttribute("resultados", resultados);
        model.addAttribute("origem", origem);
        model.addAttribute("destino", destino);
        model.addAttribute("dataIda", dataIda);
        model.addAttribute("totalResultados", resultados.size());
        
        return "direct/resultados-ida";
    }

    // Método auxiliar para extrair detalhes de um trecho (leg)
    private void extractLegDetails(JsonNode segmentsNode, Map<String, Object> resultado, String legKeySuffix) {
        String companhia = "N/A";
        String partida = "N/A";
        String chegada = "N/A";
        long totalDurationMillis = 0;
        int tripSegmentsCount = 0;
        String primeiroAeroportoPartida = "N/A";
        String ultimoAeroportoChegada = "N/A";


        if (segmentsNode.isArray() && segmentsNode.size() > 0) {
            boolean firstTripSegmentProcessed = false;
            JsonNode lastTripSegmentForArrival = null;

            for (JsonNode segment : segmentsNode) {
                if ("TripSegment".equals(segment.at("/__typename").asText())) {
                    if (!firstTripSegmentProcessed) {
                        companhia = segment.at("/marketingCarrier/code").asText(segment.at("/operatingCarrier/code").asText("N/A"));
                        partida = segment.at("/departuredAt").asText("N/A");
                        primeiroAeroportoPartida = segment.at("/origin/code").asText("N/A");
                        firstTripSegmentProcessed = true;
                    }
                    lastTripSegmentForArrival = segment; // Keep track of the latest trip segment for arrival info
                    totalDurationMillis += segment.at("/duration").asLong(0); // duration em milissegundos
                    tripSegmentsCount++;
                }
            }
            if (lastTripSegmentForArrival != null) {
                 chegada = lastTripSegmentForArrival.at("/arrivedAt").asText("N/A");
                 ultimoAeroportoChegada = lastTripSegmentForArrival.at("/destination/code").asText("N/A");
            }
        }

        resultado.put("companhia" + legKeySuffix, companhia);
        resultado.put("partida" + legKeySuffix, partida);
        resultado.put("chegada" + legKeySuffix, chegada);
        resultado.put("duracao" + legKeySuffix, (int) (totalDurationMillis / 60000)); // Convertendo para minutos
        resultado.put("escalas" + legKeySuffix, Math.max(0, tripSegmentsCount - 1));
        
        // Adiciona URL do logo da companhia aérea
        String logoUrl = AIRLINE_LOGOS.getOrDefault(companhia, DEFAULT_LOGO);
        resultado.put("logoUrl" + legKeySuffix, logoUrl);
    }
} 