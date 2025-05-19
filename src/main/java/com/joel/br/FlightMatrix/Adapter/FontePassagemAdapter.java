package com.joel.br.FlightMatrix.Adapter;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.Passagem;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface para adaptadores de fontes de passagens aéreas
 * Permite a integração com diferentes APIs e serviços de busca de passagens
 */
public interface FontePassagemAdapter extends AutoCloseable {
    
    /**
     * Retorna o nome do adaptador/fonte
     */
    String getNome();
    
    /**
     * Verifica se o adaptador está operacional
     */
    boolean isOperacional();
    
    /**
     * Busca passagens só de ida entre dois aeroportos para uma data específica
     * 
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIda Data de ida
     * @return Lista de passagens encontradas
     */
    List<Passagem> buscarPassagensIda(Aeroporto origem, Aeroporto destino, LocalDate dataIda);
    
    /**
     * Busca passagens de ida e volta entre dois aeroportos para datas específicas
     * 
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIda Data de ida
     * @param dataVolta Data de volta
     * @return Lista de passagens encontradas
     */
    List<Passagem> buscarPassagensIdaVolta(Aeroporto origem, Aeroporto destino, 
                                        LocalDate dataIda, LocalDate dataVolta);

    /**
     * Busca passagens de ida dentro de um período flexível para os aeroportos especificados
     *
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIdaMinima Data mínima de partida
     */
    List<Passagem> buscarPassagensIdaFlexivel(Aeroporto origem, Aeroporto destino,
                                              LocalDate dataIdaMinima, LocalDate dataIdaMaxima);

    /**
     * Busca passagens de ida e volta dentro de períodos flexíveis para os aeroportos especificados
     *
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIdaMinima Data mínima de partida
     * @param dataIdaMaxima Data máxima de partida
     * @param dataVoltaMinima Data mínima de retorno
     * @param dataVoltaMaxima Data máxima de retorno
     * @return Lista de passagens encontradas
     */
    List<Passagem> buscarPassagensIdaVoltaFlexivel(Aeroporto origem, Aeroporto destino,
                                                   LocalDate dataIdaMinima, LocalDate dataIdaMaxima,
                                                   LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima);
}

