package com.joel.br.FlightMatrix.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassagemExternaDTO {

    @JsonProperty("price")
    @JsonAlias({"preco", "valor", "fare"})
    private String preco;

    @JsonProperty("company")
    @JsonAlias({"companhia", "airline", "companhiaAerea"})
    private String companhia;

    @JsonProperty("stops")
    @JsonAlias({"escalas", "conexoes", "layovers"})
    private Integer escalas;

    @JsonProperty("url")
    @JsonAlias({"link", "bookingUrl"})
    private String url;

    @JsonProperty("departureDate")
    @JsonAlias({"dataIda", "ida", "departure"})
    private String dataIdaStr;

    @JsonProperty("returnDate")
    @JsonAlias({"dataVolta", "volta", "return"})
    private String dataVoltaStr;

    @JsonProperty("departureTime")
    @JsonAlias({"horaIda", "horaSaida"})
    private String horaIdaStr;

    @JsonProperty("returnTime")
    @JsonAlias({"horaVolta", "horaChegada"})
    private String horaVoltaStr;

    @JsonProperty("flightClass")
    @JsonAlias({"classe", "cabine"})
    private String classe;

    @JsonProperty("flightNumber")
    @JsonAlias({"numeroVoo", "voo"})
    private String numeroVoo;

    @JsonProperty("duration")
    @JsonAlias({"duracao", "tempoVoo"})
    private String duracaoStr;

    @JsonProperty("previousPrice")
    @JsonAlias({"precoAnterior", "valorAntigo"})
    private String precoAnterior;
}