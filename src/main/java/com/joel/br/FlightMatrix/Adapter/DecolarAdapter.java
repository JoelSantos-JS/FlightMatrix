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
 * Implementação do adaptador para a fonte Decolar.com
 */
@Slf4j
public class DecolarAdapter implements FontePassagemAdapter {

    private static final String API_BASE_URL = "https://www.decolar.com/shop/flights-api/search";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FontePassagem fonte;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DecolarAdapter(FontePassagem fonte) {
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
        // Neste exemplo, para simplicidade, buscamos apenas na data mínima
        // Em uma implementação real, faríamos buscas para cada data no período
        return buscarPassagensIda(origem, destino, dataIdaMinima);
    }

    @Override
    public List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino,
                                                          LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                                          LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        // Neste exemplo, para simplicidade, buscamos apenas nas datas mínimas
        // Em uma implementação real, faríamos buscas para cada combinação de datas
        return buscarPassagensIdaVolta(origem, destino, dataIdaMinima, dataVoltaMinima);
    }

    @Override
    public boolean isOperacional() {
        // Verificação simples se a fonte está ativa
        return fonte.getAtiva();
    }

    // Métodos auxiliares privados

    private String construirUrlBusca(String origem, String destino, LocalDate dataIda, LocalDate dataVolta) {
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL);
        urlBuilder.append("?site=BR&from=").append(origem);
        urlBuilder.append("&to=").append(destino);
        urlBuilder.append("&departure=").append(dataIda.format(DATE_FORMATTER));

        if (dataVolta != null) {
            urlBuilder.append("&return=").append(dataVolta.format(DATE_FORMATTER));
        }

        urlBuilder.append("&adults=1&children=0&infants=0&currency=BRL");

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
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Erro ao consultar API: {} - {}", response.code(), response.message());
                    return passagens;
                }

                String responseBody = response.body().string();

                // Este é um exemplo simulado - em um caso real, seria necessário
                // implementar a lógica de parsing específica do JSON da Decolar
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

    private Passagem converterParaPassagem(PassagemExternaDTO dto, Aeroporto origem, Aeroporto destino,
                                           LocalDate dataIda, LocalDate dataVolta) {
        // Conversão da DTO externa para a entidade Passagem
        Passagem passagem = new Passagem();
        passagem.setOrigem(origem);
        passagem.setDestino(destino);
        passagem.setDataIda(dataIda);
        passagem.setDataVolta(dataVolta);
        passagem.setPreco(new BigDecimal(dto.getPreco()));
        passagem.setCompanhiaAerea(dto.getCompanhia());
        passagem.setEscalas(dto.getEscalas());
        passagem.setMoeda("BRL");
        passagem.setUrl(dto.getUrl());
        passagem.setFonte(fonte);

        return passagem;
    }
}