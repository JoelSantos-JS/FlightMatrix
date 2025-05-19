package com.joel.br.FlightMatrix.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária para informações sobre companhias aéreas e conversão de moedas.
 */
public class AirlineUtil {
    
    // Taxa de conversão USD para BRL (valor fixo para exemplo)
    private static final BigDecimal USD_TO_BRL_RATE = new BigDecimal("5.1");
    
    // Mapa de códigos de companhias aéreas para URLs de logos
    private static final Map<String, String> AIRLINE_LOGOS;
    
    // URL padrão para logos não encontrados
    private static final String DEFAULT_LOGO = "https://logos.skyscnr.com/images/airlines/favicon/airline.png";
    
    static {
        Map<String, String> logos = new HashMap<>();
        
        // Companhias brasileiras
        logos.put("AD", "https://logos.skyscnr.com/images/airlines/favicon/AD.png"); // Azul
        logos.put("G3", "https://logos.skyscnr.com/images/airlines/favicon/G3.png"); // Gol
        logos.put("LA", "https://logos.skyscnr.com/images/airlines/favicon/LA.png"); // LATAM
        
        // Companhias internacionais
        logos.put("AA", "https://logos.skyscnr.com/images/airlines/favicon/AA.png"); // American Airlines
        logos.put("UA", "https://logos.skyscnr.com/images/airlines/favicon/UA.png"); // United Airlines
        logos.put("DL", "https://logos.skyscnr.com/images/airlines/favicon/DL.png"); // Delta
        logos.put("AF", "https://logos.skyscnr.com/images/airlines/favicon/AF.png"); // Air France
        logos.put("BA", "https://logos.skyscnr.com/images/airlines/favicon/BA.png"); // British Airways
        logos.put("IB", "https://logos.skyscnr.com/images/airlines/favicon/IB.png"); // Iberia
        logos.put("LH", "https://logos.skyscnr.com/images/airlines/favicon/LH.png"); // Lufthansa
        logos.put("KL", "https://logos.skyscnr.com/images/airlines/favicon/KL.png"); // KLM
        logos.put("EK", "https://logos.skyscnr.com/images/airlines/favicon/EK.png"); // Emirates
        logos.put("QR", "https://logos.skyscnr.com/images/airlines/favicon/QR.png"); // Qatar
        
        AIRLINE_LOGOS = Collections.unmodifiableMap(logos);
    }
    
    /**
     * Obtém a URL do logo de uma companhia aérea a partir do seu código IATA.
     * 
     * @param airlineCode Código IATA da companhia (exemplo: "LA", "G3", "AA")
     * @return URL do logo da companhia ou logo padrão se não encontrado
     */
    public static String getAirlineLogo(String airlineCode) {
        if (airlineCode == null || airlineCode.trim().isEmpty()) {
            return DEFAULT_LOGO;
        }
        
        return AIRLINE_LOGOS.getOrDefault(airlineCode.toUpperCase(), DEFAULT_LOGO);
    }
    
    /**
     * Converte um preço de USD para BRL se necessário.
     * 
     * @param price Preço a ser convertido
     * @param currency Moeda original ("USD", "BRL", etc)
     * @return Preço convertido (mesma moeda se não for USD)
     */
    public static BigDecimal convertCurrency(BigDecimal price, String currency) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        
        if ("USD".equalsIgnoreCase(currency)) {
            return price.multiply(USD_TO_BRL_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        
        return price;
    }
    
    /**
     * Formata o valor da moeda como um símbolo padrão (R$, $, etc).
     * 
     * @param currency Código da moeda ("BRL", "USD", etc)
     * @return Símbolo da moeda
     */
    public static String getCurrencySymbol(String currency) {
        if (currency == null) {
            return "R$"; // Padrão Brasil
        }
        
        switch (currency.toUpperCase()) {
            case "BRL":
                return "R$";
            case "USD":
                return "$";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            default:
                return currency;
        }
    }
} 