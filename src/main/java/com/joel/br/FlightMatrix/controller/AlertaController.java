package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.DTO.AlertaDTO;
import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.NotificacaoEnviada;
import com.joel.br.FlightMatrix.services.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para gerenciamento de alertas de preço
 */
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Slf4j
public class AlertaController {

    private final AlertaService alertaService;

    /**
     * Cria um novo alerta de preço para um usuário
     */
    @PostMapping
    public ResponseEntity<AlertaDTO> criarAlerta(@Valid @RequestBody AlertaDTO alertaDTO) {
        log.info("Requisição para criar alerta: origem={}, destino={}, preço máximo={}", 
                alertaDTO.getOrigem(), alertaDTO.getDestino(), alertaDTO.getPrecoMaximo());
        
        Alerta alerta = converterParaEntidade(alertaDTO);
        Alerta alertaSalvo = alertaService.criarAlerta(alerta);
        
        return new ResponseEntity<>(converterParaDTO(alertaSalvo), HttpStatus.CREATED);
    }

    /**
     * Atualiza um alerta existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlertaDTO> atualizarAlerta(
            @PathVariable Long id, 
            @Valid @RequestBody AlertaDTO alertaDTO) {
        
        log.info("Requisição para atualizar alerta ID {}", id);
        
        Alerta alerta = converterParaEntidade(alertaDTO);
        alerta.setId(id);
        
        Alerta alertaAtualizado = alertaService.atualizarAlerta(alerta);
        
        return ResponseEntity.ok(converterParaDTO(alertaAtualizado));
    }

    /**
     * Lista todos os alertas de um usuário
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AlertaDTO>> listarAlertasPorUsuario(@PathVariable Long usuarioId) {
        log.info("Requisição para listar alertas do usuário ID {}", usuarioId);
        
        List<Alerta> alertas = alertaService.buscarAlertasPorUsuario(usuarioId);
        
        List<AlertaDTO> alertasDTO = alertas.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(alertasDTO);
    }

    /**
     * Busca um alerta específico pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertaDTO> buscarAlerta(@PathVariable Long id) {
        log.info("Requisição para buscar alerta ID {}", id);
        
        Alerta alerta = alertaService.buscarAlertaPorId(id);
        
        return ResponseEntity.ok(converterParaDTO(alerta));
    }

    /**
     * Remove um alerta pelo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAlerta(@PathVariable Long id) {
        log.info("Requisição para remover alerta ID {}", id);
        
        alertaService.removerAlerta(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Ativa/desativa um alerta
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaDTO> alterarStatusAlerta(
            @PathVariable Long id, 
            @RequestParam boolean ativo) {
        
        log.info("Requisição para alterar status do alerta ID {} para {}", id, ativo ? "ativo" : "inativo");
        
        Alerta alerta = alertaService.alterarStatusAlerta(id, ativo);
        
        return ResponseEntity.ok(converterParaDTO(alerta));
    }

    /**
     * Lista notificações enviadas para um alerta específico
     */
    @GetMapping("/{id}/notificacoes")
    public ResponseEntity<List<LocalDateTime>> listarNotificacoes(@PathVariable Long id) {
        log.info("Requisição para listar notificações do alerta ID {}", id);
        
        List<NotificacaoEnviada> notificacoes = alertaService.buscarNotificacoesPorAlerta(id);
        
        List<LocalDateTime> datasNotificacoes = notificacoes.stream()
                .map(NotificacaoEnviada::getDataHoraEnvio)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(datasNotificacoes);
    }

    /**
     * Executa verificação manual dos alertas para um usuário
     */
    @PostMapping("/verificar/{usuarioId}")
    public ResponseEntity<Integer> verificarAlertasManualmente(@PathVariable Long usuarioId) {
        log.info("Requisição para verificação manual de alertas do usuário ID {}", usuarioId);
        
        int qtdNotificacoes = alertaService.verificarAlertasUsuario(usuarioId);
        
        return ResponseEntity.ok(qtdNotificacoes);
    }

    /**
     * Converte um DTO para entidade
     */
    private Alerta converterParaEntidade(AlertaDTO dto) {
        Alerta alerta = new Alerta();
        
        // ID é ignorado em criação, usado apenas em atualização
        
        // Usuário e aeroportos são associados pelo serviço
        
        // Datas
        alerta.setDataIdaMinima(dto.getDataIdaMinima());
        alerta.setDataIdaMaxima(dto.getDataIdaMaxima());
        alerta.setDataVoltaMinima(dto.getDataVoltaMinima());
        alerta.setDataVoltaMaxima(dto.getDataVoltaMaxima());
        
        // Configurações
        alerta.setPrecoMaximo(dto.getPrecoMaximo());
        alerta.setTempoMinimoPermanencia(dto.getTempoMinimoPermanencia());
        alerta.setTempoMaximoPermanencia(dto.getTempoMaximoPermanencia());
        alerta.setEscalasMaximas(dto.getEscalasMaximas());
        alerta.setCompanhiasAereas(dto.getCompanhiasAereas());
        
        // Status
        alerta.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        
        return alerta;
    }

    /**
     * Converte uma entidade para DTO
     */
    private AlertaDTO converterParaDTO(Alerta alerta) {
        AlertaDTO dto = new AlertaDTO();
        
        dto.setId(alerta.getId());
        
        if (alerta.getUsuario() != null) {
            dto.setUsuarioId(alerta.getUsuario().getId());
        }
        
        if (alerta.getOrigem() != null) {
            dto.setOrigem(alerta.getOrigem().getCodigo());
        }
        
        if (alerta.getDestino() != null) {
            dto.setDestino(alerta.getDestino().getCodigo());
        }
        
        // Datas
        dto.setDataIdaMinima(alerta.getDataIdaMinima());
        dto.setDataIdaMaxima(alerta.getDataIdaMaxima());
        dto.setDataVoltaMinima(alerta.getDataVoltaMinima());
        dto.setDataVoltaMaxima(alerta.getDataVoltaMaxima());
        
        // Configurações
        dto.setPrecoMaximo(alerta.getPrecoMaximo());
        dto.setTempoMinimoPermanencia(alerta.getTempoMinimoPermanencia());
        dto.setTempoMaximoPermanencia(alerta.getTempoMaximoPermanencia());
        dto.setEscalasMaximas(alerta.getEscalasMaximas());
        dto.setCompanhiasAereas(alerta.getCompanhiasAereas());
        
        // Status
        dto.setAtivo(alerta.getAtivo());
        dto.setUltimaNotificacao(alerta.getUltimaNotificacao());
        
        return dto;
    }
} 