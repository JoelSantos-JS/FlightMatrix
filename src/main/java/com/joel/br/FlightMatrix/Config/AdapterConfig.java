package com.joel.br.FlightMatrix.Config;

import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.repository.FontePassagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdapterConfig {

    private final FontePassagemRepository fontePassagemRepository;

    /**
     * Inicializa as fontes padrão no sistema quando a aplicação é iniciada
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Inicializando fontes de passagens aéreas...");

        // Verifica se já existem fontes cadastradas
        long fontesCadastradas = fontePassagemRepository.count();

        if (fontesCadastradas == 0) {
            log.info("Nenhuma fonte cadastrada. Inicializando fontes padrão...");
            inicializarFontesPadrao();
        } else {
            log.info("Fontes já inicializadas: {} fontes cadastradas", fontesCadastradas);
        }
    }

    /**
     * Inicializa as fontes padrão de passagens aéreas
     */
    private void inicializarFontesPadrao() {
        // Adicionar fonte Decolar
        FontePassagem decoar = new FontePassagem();
        decoar.setNome("decolar");
        decoar.setUrl("https://www.decolar.com");
        decoar.setTipo("API");
        decoar.setAtiva(true);
        decoar.setConfiguracao("{\"user_agent\": \"Mozilla/5.0\", \"timeout\": 30}");

        // Adicionar fonte MaxMilhas
        FontePassagem maxMilhas = new FontePassagem();
        maxMilhas.setNome("maxmilhas");
        maxMilhas.setUrl("https://www.maxmilhas.com.br");
        maxMilhas.setTipo("API");
        maxMilhas.setAtiva(true);
        maxMilhas.setConfiguracao("{\"user_agent\": \"Mozilla/5.0\", \"timeout\": 30}");

        // Adicionar fonte 123Milhas
        FontePassagem milhas123 = new FontePassagem();
        milhas123.setNome("123milhas");
        milhas123.setUrl("https://123milhas.com");
        milhas123.setTipo("API_SCRAPING");
        milhas123.setAtiva(true);
        milhas123.setConfiguracao("{\"user_agent\": \"Mozilla/5.0\", \"timeout\": 30, \"scraping_enabled\": true}");

        // Salvar todas as fontes
        List<FontePassagem> fontes = List.of(decoar, maxMilhas, milhas123);
        fontePassagemRepository.saveAll(fontes);

        log.info("Fontes padrão inicializadas com sucesso: {} fontes cadastradas", fontes.size());

        // Testar adaptadores

    }

    /**
     * Testa se os adaptadores podem ser criados corretamente
     */

}