package com.joel.br.FlightMatrix.Factory;

import com.joel.br.FlightMatrix.Adapter.FontePassagemAdapter;
import com.joel.br.FlightMatrix.Adapter.VibeBookingAdapter;
import com.joel.br.FlightMatrix.models.FontePassagem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Fábrica para criar instâncias do adaptador Vibe Booking
 */
@Component
@Slf4j
public class VibeBookingAdapterFactory {

    @Value("${flightmatrix.api.vibebooking.key:}")
    private String apiKey;
    
    /**
     * Cria um adaptador para a Vibe Booking com a configuração apropriada
     * 
     * @param fonte Fonte de passagem configurada no sistema
     * @return Adaptador configurado ou null se não for possível criar
     */
    public FontePassagemAdapter createAdapter(FontePassagem fonte) {
        if (fonte == null || !fonte.getAtiva()) {
            return null;
        }
        
        if (!"VIBEBOOKING".equalsIgnoreCase(fonte.getNome())) {
            return null;
        }
        
        // Verifica se a chave da API está configurada
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Chave de API para Vibe Booking não configurada. Configure a propriedade flightmatrix.api.vibebooking.key");
            return null;
        }
        
        log.info("Criando adaptador para Vibe Booking");
        return new VibeBookingAdapter(fonte, apiKey);
    }
} 