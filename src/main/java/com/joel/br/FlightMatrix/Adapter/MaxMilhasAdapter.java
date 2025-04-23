package com.joel.br.FlightMatrix.Adapter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.joel.br.FlightMatrix.DTO.PassagemExternaDTO;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementação do adaptador para a fonte MaxMilhas
 */
@Slf4j
public class MaxMilhasAdapter implements FontePassagemAdapter {

    private static final String API_BASE_URL = "https://api.maxmilhas.com.br/search/flights";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FontePassagem fonte;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MaxMilhasAdapter(FontePassagem fonte) {
        this.fonte = fonte;
        this.objectMapper = new ObjectMapper();

        // Configuração do cliente HTTP com timeout adequado
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getNome() {
        return fonte.getNome();
    }

    @Override
    public List<Passagem> buscarPassagensIda(Aeroporto origem, Aeroporto destino, LocalDate dataIda) {
        String url = construirUrlBusca(origem.getCodigo(), destino.getCodigo(), dataIda, null);
        log.info("Buscando passagens de ida em {}: {} -> {} data {}", fonte.getNome(), origem.getCodigo(),
                destino.getCodigo(), dataIda);

        return executarBusca(url, origem, destino, dataIda, null);
    }

    @Override
    public List<Passagem> buscarPassagensIdaVolta(Aeroporto origem, Aeroporto destino, LocalDate dataIda, LocalDate dataVolta) {
        String url = construirUrlBusca(origem.getCodigo(), destino.getCodigo(), dataIda, dataVolta);
        log.info("Buscando passagens de ida e volta em {}: {} -> {} data ida {} volta {}",
                fonte.getNome(), origem.getCodigo(), destino.getCodigo(), dataIda, dataVolta);

        return executarBusca(url, origem, destino, dataIda, dataVolta);
    }

    @Override
    public List<Passagem> buscarPassagensIdaFlexivel(Aeroporto origem, Aeroporto destino,
                                                     LocalDate dataIdaMinima, LocalDate dataIdaMaxima) {
        // Implementação para busca com datas flexíveis
        // Para MaxMilhas, podemos usar o parâmetro de flexibilidade da API
        String url = String.format("%s?from=%s&to=%s&departureInitDate=%s&departureEndDate=%s&flexible=true",
                API_BASE_URL,
                origem.getCodigo(),
                destino.getCodigo(),
                dataIdaMinima.format(DATE_FORMATTER),
                dataIdaMaxima.format(DATE_FORMATTER));

        log.info("Buscando passagens flexíveis em {}: {} -> {} de {} a {}",
                fonte.getNome(), origem.getCodigo(), destino.getCodigo(), dataIdaMinima, dataIdaMaxima);

        return executarBuscaFlexivel(url, origem, destino, dataIdaMinima, dataIdaMaxima, null, null);
    }

    @Override
    public List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino,
                                                          LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                                          LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        // Implementação para busca com datas flexíveis de ida e volta
        String url = String.format("%s?from=%s&to=%s&departureInitDate=%s&departureEndDate=%s&returnInitDate=%s&returnEndDate=%s&flexible=true",
                API_BASE_URL,
                origem.getCodigo(),
                destino.getCodigo(),
                dataIdaMinima.format(DATE_FORMATTER),
                dataIdaMaxima.format(DATE_FORMATTER),
                dataVoltaMinima.format(DATE_FORMATTER),
                dataVoltaMaxima.format(DATE_FORMATTER));

        log.info("Buscando passagens flexíveis de ida e volta em {}: {} -> {} de {} a {} volta de {} a {}",
                fonte.getNome(), origem.getCodigo(), destino.getCodigo(),
                dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);

        return executarBuscaFlexivel(url, origem, destino, dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);
    }

    @Override
    public boolean isOperacional() {
        return fonte.getAtiva();
    }

    // Métodos auxiliares privados

    private String construirUrlBusca(String origem, String destino, LocalDate dataIda, LocalDate dataVolta) {
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL);
        urlBuilder.append("?from=").append(origem);
        urlBuilder.append("&to=").append(destino);
        urlBuilder.append("&departureDate=").append(dataIda.format(DATE_FORMATTER));

        if (dataVolta != null) {
            urlBuilder.append("&returnDate=").append(dataVolta.format(DATE_FORMATTER));
        }

        urlBuilder.append("&adults=1&children=0&infants=0&cabinType=ECONOMIC");

        return urlBuilder.toString();
    }

    private List<Passagem> executarBusca(String url, Aeroporto origem, Aeroporto destino,
                                         LocalDate dataIda, LocalDate dataVolta) {
        List<Passagem> passagens = new ArrayList<>();

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + obterTokenAutenticacao())
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Erro ao consultar API MaxMilhas: {} - {}", response.code(), response.message());
                    return passagens;
                }

                String responseBody = response.body().string();

                // Simulação de processamento do JSON da MaxMilhas
                PassagemExternaDTO[] resultados = objectMapper.readValue(responseBody, PassagemExternaDTO[].class);

                for (PassagemExternaDTO dto : resultados) {
                    Passagem passagem = converterParaPassagem(dto, origem, destino, dataIda, dataVolta);
                    passagens.add(passagem);
                }
            }
        } catch (IOException e) {
            log.error("Erro ao executar busca em {}: {}", fonte.getNome(), e.getMessage(), e);
        }

        return passagens;
    }

    private List<Passagem> executarBuscaFlexivel(String url, Aeroporto origem, Aeroporto destino,
                                                 LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                                 LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        // Similar à busca normal, mas processando múltiplas datas
        // Na implementação real, seria necessário processar as várias opções retornadas
        // Para este exemplo, vamos retornar uma implementação simplificada

        List<Passagem> passagens = new ArrayList<>();

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + obterTokenAutenticacao())
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Erro ao consultar API MaxMilhas: {} - {}", response.code(), response.message());
                    return passagens;
                }

                String responseBody = response.body().string();

                // Processamento da resposta flexível (simulado)
                // Em um caso real, seria necessário analisar várias combinações de datas
                PassagemExternaDTO[] resultados = objectMapper.readValue(responseBody, PassagemExternaDTO[].class);

                for (PassagemExternaDTO dto : resultados) {
                    // Assumindo que a API retorna a data específica para cada opção
                    LocalDate dataIdaEfetiva = dataIdaMinima; // Substitua pela data da resposta em um caso real
                    LocalDate dataVoltaEfetiva = dataVoltaMinima; // Substitua pela data da resposta em um caso real

                    Passagem passagem = converterParaPassagem(dto, origem, destino, dataIdaEfetiva, dataVoltaEfetiva);
                    passagens.add(passagem);
                }
            }
        } catch (IOException e) {
            log.error("Erro ao executar busca flexível em {}: {}", fonte.getNome(), e.getMessage(), e);
        }

        return passagens;
    }

    private String obterTokenAutenticacao() {
        // Na implementação real, seria necessário obter o token de autenticação da API
        // Para este exemplo, estamos retornando um token fictício
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkZsaWdodE1hdHJpeCIsImlhdCI6MTUxNjIzOTAyMn0.abc123";
    }

    private Passagem converterParaPassagem(PassagemExternaDTO dto, Aeroporto origem, Aeroporto destino,
                                           LocalDate dataIda, LocalDate dataVolta) {
        // Conversão da DTO externa para a entidade Passagem
        Passagem passagem = new Passagem();
        passagem.setOrigem(origem);
        passagem.setDestino(destino);
        passagem.setDataIda(dataIda);
        passagem.setDataVolta(dataVolta);
        passagem.setPreco(new BigDecimal(dto.getPreco()));

        // Verificar se há preço anterior (promoção)
        if (dto.getPrecoAnterior() != null && !dto.getPrecoAnterior().isEmpty()) {
            passagem.setPrecoAnterior(new BigDecimal(dto.getPrecoAnterior()));
        }

        passagem.setCompanhiaAerea(dto.getCompanhia());
        passagem.setEscalas(dto.getEscalas());
        passagem.setMoeda("BRL");
        passagem.setUrl(dto.getUrl());
        passagem.setFonte(fonte);

        return passagem;
    }
}