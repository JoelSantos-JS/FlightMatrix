package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.DTO.PassagemDTO;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.services.DealDiscoveryService;
import com.joel.br.FlightMatrix.services.PassagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para expor endpoints relacionados a passagens aéreas
 */
@RestController
@RequestMapping("/api/passagens")
@RequiredArgsConstructor
@Slf4j
public class PassagemController {

    private final PassagemService passagemService;
    private final DealDiscoveryService dealDiscoveryService;

    /**
     * Busca passagens de ida entre dois aeroportos para uma data específica
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<PassagemDTO>> buscarPassagens(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVolta) {
        
        log.info("Requisição de busca de passagens: {} -> {} (Ida: {}, Volta: {})", 
                origem, destino, dataIda, dataVolta);
        
        List<Passagem> passagens = passagemService.buscarPassagens(origem, destino, dataIda, dataVolta);
        
        List<PassagemDTO> passagensDTO = passagens.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(passagensDTO);
    }

    /**
     * Busca passagens com datas flexíveis
     */
    @GetMapping("/buscar-flexivel")
    public ResponseEntity<List<PassagemDTO>> buscarPassagensFlexiveis(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIdaMinima,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIdaMaxima,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVoltaMinima,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVoltaMaxima) {
        
        log.info("Requisição de busca flexível de passagens: {} -> {} (Ida: {} a {}, Volta: {} a {})", 
                origem, destino, dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);
        
        List<Passagem> passagens = passagemService.buscasPassagensFlexiveis(
                origem, destino, dataIdaMinima, dataIdaMaxima, dataVoltaMinima, dataVoltaMaxima);
        
        List<PassagemDTO> passagensDTO = passagens.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(passagensDTO);
    }

    /**
     * Busca ofertas imperdíveis entre dois aeroportos
     */
    @GetMapping("/ofertas")
    public ResponseEntity<List<PassagemDTO>> buscarOfertas(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        log.info("Requisição de busca de ofertas: {} -> {} (período: {} a {})", 
                origem, destino, dataInicio, dataFim);
        
        List<Passagem> ofertas = dealDiscoveryService.buscarMelhoresOfertas(
                origem, destino, dataInicio, dataFim);
        
        List<PassagemDTO> ofertasDTO = ofertas.stream()
                .map(passagem -> {
                    PassagemDTO dto = converterParaDTO(passagem);
                    dto.setPromocao(true); // Marca como promoção por ser uma oferta identificada
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ofertasDTO);
    }

    /**
     * Busca passagens com preço máximo definido
     */
    @GetMapping("/por-preco")
    public ResponseEntity<List<PassagemDTO>> buscarPorPreco(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam Double precoMaximo) {
        
        log.info("Requisição de busca por preço máximo: {} -> {} (período: {} a {}, preço máximo: {})", 
                origem, destino, dataInicio, dataFim, precoMaximo);
        
        List<Passagem> passagens = passagemService.buscarPorPreco(
                origem, destino, dataInicio, dataFim, precoMaximo);
        
        List<PassagemDTO> passagensDTO = passagens.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(passagensDTO);
    }

    /**
     * Converte uma entidade Passagem para DTO
     */
    private PassagemDTO converterParaDTO(Passagem passagem) {
        PassagemDTO dto = new PassagemDTO();
        
        dto.setId(passagem.getId());
        dto.setOrigem(passagem.getOrigem().getCodigo());
        dto.setDestino(passagem.getDestino().getCodigo());
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
        
        // Verifica se é uma promoção
        dto.setPromocao(passagemService.isPromocao(passagem));
        
        return dto;
    }
} 