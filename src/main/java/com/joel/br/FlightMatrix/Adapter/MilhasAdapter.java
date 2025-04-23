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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação do adaptador para a fonte 123Milhas
 * Esta classe utiliza uma combinação de chamadas de API e web scraping
 */
@Slf4j
public class MilhasAdapter implements FontePassagemAdapter {

    private static final String WEBSITE_BASE_URL = "https://123milhas.com/";
    private static final String API_BASE_URL = "https://123milhas.com/api/v1/search";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FontePassagem fonte;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MilhasAdapter(FontePassagem fonte) {
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
        // 123Milhas oferece uma busca flexível nativa
        String url = construirUrlBuscaFlexivel(origem.getCodigo(), destino.getCodigo(),
                dataIdaMinima, dataIdaMaxima, null, null);
        log.info("Buscando passagens flexíveis em {}: {} -> {}", fonte.getNome(), origem.getCodigo(), destino.getCodigo());

        return executarBuscaFlexivel(url, origem, destino, dataIdaMinima, dataIdaMaxima, null, null);
    }

    @Override
    public List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino,
                                                          LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                                          LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        String url = construirUrlBuscaFlexivel(origem.getCodigo(), destino.getCodigo(),
                dataIdaMinima, dataIdaMaxima,
                dataVoltaMinima, dataVoltaMaxima);
        log.info("Buscando passagens flexíveis de ida e volta em {}: {} -> {}",
                fonte.getNome(), origem.getCodigo(), destino.getCodigo());

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
        } else {
            urlBuilder.append("&tripType=ONE_WAY");
        }

        urlBuilder.append("&adults=1&children=0&infants=0");

        return urlBuilder.toString();
    }

    private String construirUrlBuscaFlexivel(String origem, String destino,
                                             LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                             LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima) {
        // 123Milhas utiliza um formato específico para busca flexível
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL);
        urlBuilder.append("/flexible?from=").append(origem);
        urlBuilder.append("&to=").append(destino);

        // Período de ida
        int diasFlexibilidadeIda = (int) java.time.temporal.ChronoUnit.DAYS.between(dataIdaMinima, dataIdaMaxima);
        urlBuilder.append("&departureDateStart=").append(dataIdaMinima.format(DATE_FORMATTER));
        urlBuilder.append("&departureDateEnd=").append(dataIdaMaxima.format(DATE_FORMATTER));
        urlBuilder.append("&departureDaysRange=").append(diasFlexibilidadeIda);

        // Período de volta (se aplicável)
        if (dataVoltaMinima != null && dataVoltaMaxima != null) {
            int diasFlexibilidadeVolta = (int) java.time.temporal.ChronoUnit.DAYS.between(dataVoltaMinima, dataVoltaMaxima);
            urlBuilder.append("&returnDateStart=").append(dataVoltaMinima.format(DATE_FORMATTER));
            urlBuilder.append("&returnDateEnd=").append(dataVoltaMaxima.format(DATE_FORMATTER));
            urlBuilder.append("&returnDaysRange=").append(diasFlexibilidadeVolta);
        } else {
            urlBuilder.append("&tripType=ONE_WAY");
        }

        urlBuilder.append("&adults=1&children=0&infants=0");

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
                    log.error("Erro ao consultar API 123Milhas: {} - {}", response.code(), response.message());

                    // Tentar alternativa com web scraping se a API falhar
                    return executarBuscaPorScraping(origem, destino, dataIda, dataVolta);
                }

                String responseBody = response.body().string();

                // Processamento do JSON da 123Milhas
                // A estrutura real seria mais complexa, isso é simplificado para o exemplo
                try {
                    // Tenta extrair os resultados diretamente como array
                    PassagemExternaDTO[] resultados = objectMapper.readValue(responseBody, PassagemExternaDTO[].class);

                    for (PassagemExternaDTO dto : resultados) {
                        Passagem passagem = converterParaPassagem(dto, origem, destino, dataIda, dataVolta);
                        passagens.add(passagem);
                    }
                } catch (Exception e) {
                    // Se não conseguir extrair diretamente, tenta extrair do objeto raiz
                    try {
                        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
                        com.fasterxml.jackson.databind.JsonNode flights = root.path("flights");

                        if (flights.isArray()) {
                            for (com.fasterxml.jackson.databind.JsonNode flight : flights) {
                                PassagemExternaDTO dto = objectMapper.treeToValue(flight, PassagemExternaDTO.class);
                                Passagem passagem = converterParaPassagem(dto, origem, destino, dataIda, dataVolta);
                                passagens.add(passagem);
                            }
                        }
                    } catch (Exception e2) {
                        log.error("Erro ao processar JSON da 123Milhas: {}", e2.getMessage(), e2);
                    }
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
        // Similar à busca regular, mas processando respostas com datas flexíveis
        List<Passagem> passagens = new ArrayList<>();

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Erro ao consultar API flexível 123Milhas: {} - {}", response.code(), response.message());
                    return passagens;
                }

                String responseBody = response.body().string();

                // Processamento do JSON flexível - lógica simplificada para o exemplo
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
                com.fasterxml.jackson.databind.JsonNode combinations = root.path("dateCombinations");

                if (combinations.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode combination : combinations) {
                        LocalDate dataIdaEfetiva = LocalDate.parse(
                                combination.path("departureDate").asText(),
                                DateTimeFormatter.ISO_DATE);

                        LocalDate dataVoltaEfetiva = null;
                        if (combination.has("returnDate") && !combination.path("returnDate").isNull()) {
                            dataVoltaEfetiva = LocalDate.parse(
                                    combination.path("returnDate").asText(),
                                    DateTimeFormatter.ISO_DATE);
                        }

                        com.fasterxml.jackson.databind.JsonNode flights = combination.path("flights");
                        if (flights.isArray()) {
                            for (com.fasterxml.jackson.databind.JsonNode flight : flights) {
                                PassagemExternaDTO dto = objectMapper.treeToValue(flight, PassagemExternaDTO.class);
                                Passagem passagem = converterParaPassagem(dto, origem, destino, dataIdaEfetiva, dataVoltaEfetiva);
                                passagens.add(passagem);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Erro ao executar busca flexível em {}: {}", fonte.getNome(), e.getMessage(), e);
        }

        return passagens;
    }

    private List<Passagem> executarBuscaPorScraping(Aeroporto origem, Aeroporto destino,
                                                    LocalDate dataIda, LocalDate dataVolta) {
        List<Passagem> passagens = new ArrayList<>();

        try {
            // Construir a URL da página de resultados
            String urlPagina = WEBSITE_BASE_URL + "busca?" +
                    "de=" + origem.getCodigo() +
                    "&para=" + destino.getCodigo() +
                    "&ida=" + dataIda.format(DISPLAY_DATE_FORMATTER);

            if (dataVolta != null) {
                urlPagina += "&volta=" + dataVolta.format(DISPLAY_DATE_FORMATTER);
            }

            urlPagina += "&adultos=1&criancas=0&bebes=0&classe=3";

            Request request = new Request.Builder()
                    .url(urlPagina)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Erro ao fazer scraping da 123Milhas: {} - {}", response.code(), response.message());
                    return passagens;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                // Exemplo de extração dos resultados por scraping
                Elements resultElements = doc.select(".flight-card");

                for (Element result : resultElements) {
                    try {
                        Passagem passagem = new Passagem();
                        passagem.setOrigem(origem);
                        passagem.setDestino(destino);
                        passagem.setDataIda(dataIda);
                        passagem.setDataVolta(dataVolta);

                        // Extrair preço
                        String precoText = result.select(".price").text();
                        Pattern precoPattern = Pattern.compile("R\\$ ([0-9.,]+)");
                        Matcher precoMatcher = precoPattern.matcher(precoText);

                        if (precoMatcher.find()) {
                            String precoStr = precoMatcher.group(1)
                                    .replace(".", "")
                                    .replace(",", ".");
                            passagem.setPreco(new BigDecimal(precoStr));
                        }

                        // Extrair companhia aérea
                        String companhia = result.select(".airline-name").text();
                        passagem.setCompanhiaAerea(companhia);

                        // Extrair número de escalas
                        String escalasText = result.select(".stops").text();
                        if (escalasText.contains("Direto")) {
                            passagem.setEscalas(0);
                        } else {
                            Pattern escalasPattern = Pattern.compile("([0-9]+)\\s+escala");
                            Matcher escalasMatcher = escalasPattern.matcher(escalasText);

                            if (escalasMatcher.find()) {
                                passagem.setEscalas(Integer.parseInt(escalasMatcher.group(1)));
                            } else {
                                passagem.setEscalas(1); // Valor padrão se não conseguir extrair
                            }
                        }

                        // Definir outros campos
                        passagem.setMoeda("BRL");

                        // Extrair URL de detalhes
                        Element linkElement = result.select("a.detail-link").first();
                        if (linkElement != null) {
                            String href = linkElement.attr("href");
                            if (!href.startsWith("http")) {
                                href = WEBSITE_BASE_URL + href;
                            }
                            passagem.setUrl(href);
                        } else {
                            passagem.setUrl(urlPagina);
                        }

                        // Verificar se é promoção
                        Element promoElement = result.select(".promo-tag").first();
                        if (promoElement != null) {
                            // Tenta extrair preço anterior
                            Element precoAnteriorElement = result.select(".original-price").first();
                            if (precoAnteriorElement != null) {
                                String precoAnteriorText = precoAnteriorElement.text();
                                Pattern precoAnteriorPattern = Pattern.compile("R\\$ ([0-9.,]+)");
                                Matcher precoAnteriorMatcher = precoAnteriorPattern.matcher(precoAnteriorText);

                                if (precoAnteriorMatcher.find()) {
                                    String precoAnteriorStr = precoAnteriorMatcher.group(1)
                                            .replace(".", "")
                                            .replace(",", ".");
                                    passagem.setPrecoAnterior(new BigDecimal(precoAnteriorStr));
                                }
                            }
                        }

                        passagem.setFonte(fonte);
                        passagens.add(passagem);
                    } catch (Exception e) {
                        log.warn("Erro ao processar elemento de resultado: {}", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Erro ao fazer scraping em {}: {}", fonte.getNome(), e.getMessage(), e);
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