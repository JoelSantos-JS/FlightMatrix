package com.joel.br.FlightMatrix.Adapter;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.Passagem;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface que define o contrato para adaptadores de fontes de passagens aéreas.
 * Cada implementação deve fornecer métodos para buscar passagens de uma fonte específica.
 */
public interface FontePassagemAdapter {

    /**
     * Retorna o nome da fonte de passagens
     * @return Nome da fonte
     */
    String getNome();

    /**
     * Busca passagens somente de ida entre dois aeroportos e período específico
     *
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIda Data de ida desejada
     * @return Lista de passagens encontradas
     */
    List<Passagem> buscarPassagensIda(Aeroporto origem, Aeroporto destino, LocalDate dataIda);

    /**
     * Busca passagens de ida e volta entre dois aeroportos e período específico
     *
     * @param origem Aeroporto de origem
     * @param destino Aeroporto de destino
     * @param dataIda Data de ida desejada
     * @param dataVolta Data de volta desejada
     * @return Lista de passagens encontradas
     */
    List<Passagem> buscarPassagensIdaVolta(Aeroporto origem, Aeroporto destino, LocalDate dataIda, LocalDate dataVolta);

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

    /**
     * Verifica se o adaptador está operacional e pode realizar buscas
     *
     * @return true se o adaptador está operacional, false caso contrário
     */
    boolean isOperacional();


 }

