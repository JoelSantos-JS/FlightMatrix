package com.joel.br.FlightMatrix.services;


import com.joel.br.FlightMatrix.Adapter.FontePassagemAdapter;
import com.joel.br.FlightMatrix.Factory.BookingDataAdapterFactory;
import com.joel.br.FlightMatrix.Factory.VibeBookingAdapterFactory;
import com.joel.br.FlightMatrix.exceptions.ResourceNotFoundException;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.HistoricoPreco;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.repository.FontePassagemRepository;
import com.joel.br.FlightMatrix.repository.HistoricoPrecoRepository;
import com.joel.br.FlightMatrix.repository.PassagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PassagemService {

    private final PassagemRepository passagemRepository;
    private final AeroportoRepository aeroportoRepository;
    private final FontePassagemRepository fontePassagemRepository;
    private final HistoricoPrecoRepository historicoPrecoRepository;
    private final BookingDataAdapterFactory bookingDataAdapterFactory;
    private final VibeBookingAdapterFactory vibeBookingAdapterFactory;

    /**
     * Busca passagens para uma data específica (ida ou ida e volta)
     */
    @Transactional
    public List<Passagem> buscarPassagens(String codigoOrigem, String codigoDestino, 
                                         LocalDate dataIda, LocalDate dataVolta) {
        log.info("Buscando passagens: {} -> {}, ida: {}, volta: {}", 
                codigoOrigem, codigoDestino, dataIda, dataVolta);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));

        List<Passagem> todasPassagens = new ArrayList<>();
        
        // Buscar em todas as fontes disponíveis
        List<FontePassagem> fontesAtivas = fontePassagemRepository.findByAtiva(true);
        
        for (FontePassagem fonte : fontesAtivas) {
            try (FontePassagemAdapter adapter = getAdapterPorFonte(fonte)) {
                List<Passagem> passagens;
                
                // Verifica se é busca só de ida ou ida/volta
                if (dataVolta == null) {
                    passagens = adapter.buscarPassagensIda(origem, destino, dataIda);
                } else {
                    passagens = adapter.buscarPassagensIdaVolta(origem, destino, dataIda, dataVolta);
                }
                
                // Adiciona à lista geral
                if (passagens != null && !passagens.isEmpty()) {
                    todasPassagens.addAll(passagens);
                    
                    // Salva histórico de preços
                    salvarHistoricoPrecos(passagens);
                }
            } catch (Exception e) {
                log.error("Erro ao buscar passagens na fonte {}: {}", 
                        fonte.getNome(), e.getMessage(), e);
            }
        }
        
        return todasPassagens;
    }

    /**
     * Busca passagens por preço
     */
    public List<Passagem> buscarPorPreco(String codigoOrigem, String codigoDestino, 
                                        LocalDate dataIdaInicio, LocalDate dataIdaFim, 
                                        double precoMaximo) {
        log.info("Buscando passagens com preço máximo {}: {} -> {}", 
                precoMaximo, codigoOrigem, codigoDestino);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));

        BigDecimal precoMaximoBD = BigDecimal.valueOf(precoMaximo);
        
        return passagemRepository.buscarPassagensPorPreco(
                origem, destino, dataIdaInicio, dataIdaFim, precoMaximoBD);
    }

    /**
     * Busca passagens com datas flexíveis (ida e ida/volta)
     */
    @Transactional
    public List<Passagem> buscasPassagensFlexiveis(String codigoOrigem, String codigoDestino,
                                                 LocalDate dataIdaInicio, LocalDate dataIdaFim,
                                                 LocalDate dataVoltaInicio, LocalDate dataVoltaFim) {
        log.info("Buscando passagens flexíveis: {} -> {}, ida: {} a {}, volta: {} a {}", 
                codigoOrigem, codigoDestino, dataIdaInicio, dataIdaFim, 
                dataVoltaInicio, dataVoltaFim);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));

        List<Passagem> todasPassagens = new ArrayList<>();
        
        // Buscar em todas as fontes disponíveis
        List<FontePassagem> fontesAtivas = fontePassagemRepository.findByAtiva(true);
        
        for (FontePassagem fonte : fontesAtivas) {
            try (FontePassagemAdapter adapter = getAdapterPorFonte(fonte)) {
                List<Passagem> passagens;
                
                // Verifica se é busca só de ida ou ida/volta
                if (dataVoltaInicio == null) {
                    passagens = adapter.buscarPassagensIdaFlexivel(
                            origem, destino, dataIdaInicio, dataIdaFim);
                } else {
                    passagens = adapter.buscarPassagensIdaVoltaFlexivel(
                            origem, destino, dataIdaInicio, dataIdaFim, 
                            dataVoltaInicio, dataVoltaFim);
                }
                
                // Adiciona à lista geral
                if (passagens != null && !passagens.isEmpty()) {
                    todasPassagens.addAll(passagens);
                    
                    // Salva histórico de preços
                    salvarHistoricoPrecos(passagens);
                }
            } catch (Exception e) {
                log.error("Erro ao buscar passagens flexíveis na fonte {}: {}", 
                        fonte.getNome(), e.getMessage(), e);
            }
        }
        
        return todasPassagens;
    }

    /**
     * Busca passagens por preço máximo (mantido por compatibilidade)
     */
    public List<Passagem> buscarPorPreço(String codigoOrigem, String codigoDestino, 
                                        LocalDate dataIdaInicio, LocalDate dataIdaFim, 
                                        BigDecimal precoMaximo) {
        log.info("Buscando passagens preço máximo: {} -> {}", codigoOrigem, codigoDestino);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));

        return passagemRepository.buscarPassagensPorPreco(
                origem, destino, dataIdaInicio, dataIdaFim, precoMaximo);
    }

    /**
     * Obtém o adapter adequado para a fonte de passagem
     */
    private FontePassagemAdapter getAdapterPorFonte(FontePassagem fonte) {
        switch (fonte.getNome().toLowerCase()) {
            case "bookingdata":
                return bookingDataAdapterFactory.createAdapter(fonte);
            case "vibebooking":
                return vibeBookingAdapterFactory.createAdapter(fonte);
            default:
                throw new IllegalArgumentException("Fonte de passagem não suportada: " + fonte.getNome());
        }
    }

    public boolean isPromocao(Passagem passagem) {
        if(passagem.getPrecoAnterior() != null && passagem.getPreco().compareTo(passagem.getPrecoAnterior()) < 0){
            return true;
        }

        // Buscar Historico dos ultimos 30 dias
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(30);

        List<HistoricoPreco> historicos = historicoPrecoRepository.findHistoricoPeriodicoByRota(
                passagem.getOrigem(), passagem.getDestino(), dataInicio, LocalDateTime.now());

        if(historicos.isEmpty()) {
            return false;
        }

        BigDecimal precoMedio = historicos.stream().map(HistoricoPreco::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicos.size()), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal limitePromocao = precoMedio.multiply(new BigDecimal("0.8"));

        return passagem.getPreco().compareTo(limitePromocao) <= 0;
    }

    @Transactional
    public void salvarHistoricoPrecos(List<Passagem> passagens) {
        List<HistoricoPreco> historicos = passagens.stream()
                .map(this::criarHistoricoPreco)
                .collect(Collectors.toList());

        historicoPrecoRepository.saveAll(historicos);
    }

    private HistoricoPreco criarHistoricoPreco(Passagem passagem) {
        HistoricoPreco historico = new HistoricoPreco();
        historico.setOrigem(passagem.getOrigem());
        historico.setDestino(passagem.getDestino());
        historico.setCompanhiaAerea(passagem.getCompanhiaAerea());
        historico.setPreco(passagem.getPreco());
        historico.setDataHoraConsulta(passagem.getDataHoraConsulta());
        historico.setMoeda(passagem.getMoeda());
        historico.setFonte(passagem.getFonte());

        return historico;
    }
}
