package com.joel.br.FlightMatrix.Factory;

import com.joel.br.FlightMatrix.Adapter.BookingDataAdapter;
import com.joel.br.FlightMatrix.Adapter.FontePassagemAdapter;
import com.joel.br.FlightMatrix.models.FontePassagem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Fábrica para criar instâncias do adaptador BookingData
 */
@Component
@Slf4j
public class BookingDataAdapterFactory {

    @Value("${flightmatrix.api.bookingdata.key:}")
    private String apiKey;
    
    /**
     * Cria um adaptador para a API BookingData com a configuração apropriada
     * 
     * @param fonte Fonte de passagem configurada no sistema
     * @return Adaptador configurado ou null se não for possível criar
     */
    public FontePassagemAdapter createAdapter(FontePassagem fonte) {
        if (fonte == null || !fonte.getAtiva()) {
            return null;
        }
        
        if (!"BOOKINGDATA".equalsIgnoreCase(fonte.getNome())) {
            return null;
        }
        
        // Verifica se a chave da API está configurada
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Chave de API para BookingData não configurada. Configure a propriedade flightmatrix.api.bookingdata.key");
            return null;
        }
        
        log.info("Criando adaptador para BookingData");
        return new BookingDataAdapter(fonte, apiKey);
    }
} 