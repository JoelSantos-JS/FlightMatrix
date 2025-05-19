package com.joel.br.FlightMatrix.Config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuração do sistema de cache
 * Utiliza cache em memória para armazenar resultados de buscas frequentes
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                // Cache para aeroportos (raramente mudam)
                new ConcurrentMapCache("aeroportos"),
                
                // Cache para resultados de busca (curta duração)
                new ConcurrentMapCache("buscaPassagens"),
                
                // Cache para histórico de preços (média duração)
                new ConcurrentMapCache("historicoPrecos"),
                
                // Cache para melhores ofertas (curta duração)
                new ConcurrentMapCache("melhoresOfertas"),
                
                // Cache para status de APIs externas (curta duração)
                new ConcurrentMapCache("statusApis")
        ));
        return cacheManager;
    }
} 