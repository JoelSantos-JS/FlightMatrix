package com.joel.br.FlightMatrix.services;


import com.joel.br.FlightMatrix.Factory.FontePassagemFactory;
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

import java.awt.*;
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
    private final FontePassagemFactory fontePassagemFactory;



    public List<Passagem> buscarPassagens(String codigoOrigem, String codigoDestino, LocalDate dataIda, LocalDate dataVolta) {

        log.info("Buscando passagens: {} -> {} (Ida: {}, Volta: {})", codigoOrigem, codigoOrigem, dataIda, dataVolta);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem).orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));


        Aeroporto destino = aeroportoRepository.findById(codigoDestino).orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));


        List<Passagem> todasPassagens = new ArrayList<>();

        List<FontePassagem> fonteAtivas = fontePassagemRepository.findByAtiva(true);


        for(FontePassagem fontePassagem : fonteAtivas) {
            fontePassagemFactory.criarAdapter(fontePassagem).ifPresent(adapter -> {
                List<Passagem> passagensDaFonte;


                if(dataVolta == null) {
                    passagensDaFonte = adapter.buscarPassagensIda(origem, destino, dataIda);

                }else  {
                    passagensDaFonte = adapter.buscarPassagensIdaVolta(origem, destino ,dataIda ,dataVolta);
                }

                todasPassagens.addAll(passagensDaFonte);
            });
        }


        if(!todasPassagens.isEmpty()) {
            passagemRepository.saveAll(todasPassagens);
            salvarHistoricoPrecos(todasPassagens);
        }


        return todasPassagens.stream().sorted(Comparator.comparing(Passagem::getPreco)).collect(Collectors.toList());

    }


    public List<Passagem> buscasPassagensFlexiveis(String codigoOrigem,
                                                   String codigoDestino, LocalDate dataIdaMinima, LocalDate dataIdaMaxima, LocalDate dataVoltaMinima, LocalDate dataVoltaMaxima){

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem).orElseThrow(() -> new ResourceNotFoundException("Aeroporto de Origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino).orElseThrow(() -> new ResourceNotFoundException("Aeropot de Destino não encontrado"));

        List<Passagem> todasPassagens = new ArrayList<>();

        List<FontePassagem> fontesAtivas = fontePassagemRepository.findByAtiva(true);


        for (FontePassagem fonte : fontesAtivas) {
            fontePassagemFactory.criarAdapter(fonte).ifPresent(adapter -> {
                List<Passagem> passagensDaFonte;

                if (dataVoltaMinima == null) {
                    passagensDaFonte = adapter.buscarPassagensIdaFlexivel(
                            origem, destino, dataIdaMinima, dataIdaMaxima);
                } else {
                    passagensDaFonte = adapter.buscarPassagensIdaVoltaFlexivel(
                            origem, destino, dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);
                }

                todasPassagens.addAll(passagensDaFonte);
            });
        }

        // Salva as passagens encontradas
        if (!todasPassagens.isEmpty()) {
            passagemRepository.saveAll(todasPassagens);
            salvarHistoricoPrecos(todasPassagens);
        }

        return todasPassagens.stream()
                .sorted(Comparator.comparing(Passagem::getPreco))
                .collect(Collectors.toList());
    }

    public List<Passagem> buscarPorPreço(String codigoOrigem, String codigoDestino, LocalDate dataIdaInicio, LocalDate dataIdaFim, BigDecimal precoMaximo) {

        log.info("Buscando passagens preço maximo: {} -> {}" , codigoOrigem,codigoDestino,precoMaximo);

        Aeroporto origem = aeroportoRepository.findById(codigoOrigem).orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));

        Aeroporto destino = aeroportoRepository.findById(codigoDestino).orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));;


                return passagemRepository.buscarPassagensPorPreco(origem,destino,dataIdaInicio,dataIdaFim,precoMaximo);
    }


    public boolean isPromocao(Passagem passagem ) {
        if(passagem.getPrecoAnterior() != null && passagem.getPreco().compareTo(passagem.getPrecoAnterior()) < 0){
            return  true;
        }


        // Buscar Historico dos ultimos 30 dias

        LocalDateTime dataInicio = LocalDateTime.now().minusDays(30);

        List<HistoricoPreco> historicos = historicoPrecoRepository.findHistoricoPeriodicoByRota(passagem.getOrigem(), passagem.getDestino(),dataInicio, LocalDateTime.now());


        if(historicos.isEmpty()) {
            return false;
        }

        BigDecimal precoMedio = historicos.stream().map(HistoricoPreco::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicos.size()), BigDecimal.ROUND_HALF_UP);



        BigDecimal limitePromocao =  precoMedio.multiply(new BigDecimal("0.8"));


        return  passagem.getPreco().compareTo(limitePromocao) <= 0;

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
