package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.Adapter.BookingDataAdapter;
import com.joel.br.FlightMatrix.DTO.PassagemDTO;
import com.joel.br.FlightMatrix.Factory.BookingDataAdapterFactory;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.FontePassagem;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.repository.FontePassagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para testes de integração com APIs externas
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final BookingDataAdapterFactory bookingDataAdapterFactory;
    private final AeroportoRepository aeroportoRepository;
    private final FontePassagemRepository fontePassagemRepository;

    /**
     * Endpoint para testar a API BookingData
     */
    @GetMapping("/booking-data")
    public ResponseEntity<List<PassagemDTO>> testBookingData(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVolta) {
        
        log.info("Testando BookingData API: {} -> {} (Ida: {}, Volta: {})", 
                origem, destino, dataIda, dataVolta);
        
        // Busca os aeroportos pelo código
        Aeroporto aeroportoOrigem = getOrCreateAeroporto(origem);
        Aeroporto aeroportoDestino = getOrCreateAeroporto(destino);
        
        // Cria ou busca a fonte BookingData
        FontePassagem fonteBookingData = getOrCreateFonteBookingData();
        
        // Cria o adaptador
        BookingDataAdapter adapter = (BookingDataAdapter) bookingDataAdapterFactory.createAdapter(fonteBookingData);
        
        if (adapter == null) {
            log.error("Não foi possível criar o adaptador BookingData");
            return ResponseEntity.badRequest().build();
        }
        
        List<Passagem> passagens = new ArrayList<>();
        
        try {
            // Executa a busca
            if (dataVolta == null) {
                passagens = adapter.buscarPassagensIda(aeroportoOrigem, aeroportoDestino, dataIda);
            } else {
                passagens = adapter.buscarPassagensIdaVolta(aeroportoOrigem, aeroportoDestino, dataIda, dataVolta);
            }
            
            log.info("Encontradas {} passagens", passagens.size());
            
            // Converte para DTOs
            List<PassagemDTO> passagensDTO = passagens.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(passagensDTO);
        } catch (Exception e) {
            log.error("Erro ao testar BookingData API", e);
            return ResponseEntity.internalServerError().build();
        } finally {
            try {
                adapter.close();
            } catch (Exception e) {
                log.error("Erro ao fechar o adaptador", e);
            }
        }
    }
    
    /**
     * Busca ou cria um aeroporto pelo código
     */
    private Aeroporto getOrCreateAeroporto(String codigo) {
        return aeroportoRepository.findById(codigo)
                .orElseGet(() -> {
                    Aeroporto aeroporto = new Aeroporto();
                    aeroporto.setCodigo(codigo);
                    aeroporto.setNome("Aeroporto " + codigo);
                    aeroporto.setCidade("Cidade " + codigo);
                    aeroporto.setPais("País");
                    aeroporto.setPrincipal(true);
                    return aeroportoRepository.save(aeroporto);
                });
    }
    
    /**
     * Busca ou cria uma fonte BookingData
     */
    private FontePassagem getOrCreateFonteBookingData() {
        return fontePassagemRepository.findByNome("BOOKINGDATA")
                .orElseGet(() -> {
                    FontePassagem fonte = new FontePassagem();
                    fonte.setNome("BOOKINGDATA");
                    fonte.setDescricao("API BookingData");
                    fonte.setAtiva(true);
                    fonte.setUrl("https://booking-data.p.rapidapi.com");
                    return fontePassagemRepository.save(fonte);
                });
    }
    
    /**
     * Converte uma entidade Passagem para DTO
     */
    private PassagemDTO converterParaDTO(Passagem passagem) {
        PassagemDTO dto = new PassagemDTO();
        
        if (passagem.getId() != null) {
            dto.setId(passagem.getId());
        }
        
        if (passagem.getOrigem() != null) {
            dto.setOrigem(passagem.getOrigem().getCodigo());
        }
        
        if (passagem.getDestino() != null) {
            dto.setDestino(passagem.getDestino().getCodigo());
        }
        
        dto.setDataIda(passagem.getDataIda());
        dto.setDataVolta(passagem.getDataVolta());
        dto.setPreco(passagem.getPreco());
        dto.setPrecoAnterior(passagem.getPrecoAnterior());
        dto.setCompanhiaAerea(passagem.getCompanhiaAerea());
        dto.setEscalas(passagem.getEscalas());
        dto.setDataHoraConsulta(passagem.getDataHoraConsulta());
        dto.setMoeda(passagem.getMoeda());
        dto.setUrl(passagem.getUrl());
        
        if (passagem.getFonte() != null) {
            dto.setFonte(passagem.getFonte().getNome());
        }
        
        return dto;
    }
} 