package com.joel.br.FlightMatrix.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para mapear a resposta da API BookingData
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassagemExternaDTO {
    
    @JsonProperty("data")
    private List<ResultadoVooDTO> resultados;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultadoVooDTO {
        
        @JsonProperty("price")
        private BigDecimal preco;
        
        @JsonProperty("currency")
        private String moeda;
        
        @JsonProperty("stops")
        private Integer quantidadeEscalas;
        
        @JsonProperty("bookingLink")
        private String urlReserva;
        
        @JsonProperty("duration")
        private DuracaoDTO duracao;
        
        @JsonProperty("legs")
        private List<TrechoDTO> trechos;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DuracaoDTO {
        
        @JsonProperty("total")
        private Integer totalMinutos;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrechoDTO {
        
        @JsonProperty("departure")
        private String partida;
        
        @JsonProperty("arrival")
        private String chegada;
        
        @JsonProperty("airportFrom")
        private AeroportoDTO origem;
        
        @JsonProperty("airportTo")
        private AeroportoDTO destino;
        
        @JsonProperty("flightNumber")
        private String numeroVoo;
        
        @JsonProperty("airline")
        private String companhiaAerea;
        
        @JsonProperty("operatingAirline")
        private String companhiaOperadora;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AeroportoDTO {
        
        @JsonProperty("code")
        private String codigo;
        
        @JsonProperty("name")
        private String nome;
        
        @JsonProperty("cityName")
        private String cidade;
        
        @JsonProperty("countryName")
        private String pais;
    }
}