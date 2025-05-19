package com.joel.br.FlightMatrix.Scheduler;


import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.models.Usuario;
import com.joel.br.FlightMatrix.repository.PassagemRepository;
import com.joel.br.FlightMatrix.repository.UsuarioRepository;
import com.joel.br.FlightMatrix.services.NotificacaoService;
import com.joel.br.FlightMatrix.services.PassagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agendador para envio de resumos diários com promoções.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResumoPromocoesScheduler {

    private final PassagemRepository passagemRepository;
    private final PassagemService passagemService;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    /**
     * Envia resumo diário de promoções ao meio-dia
     */
    @Scheduled(cron = "${app.scheduler.resumo-diario.cron:0 0 12 * * ?}")
    public void enviarResumoDiario() {
        log.info("Iniciando envio de resumo diário de promoções");

        // Busca todos os usuários ativos
        List<Usuario> usuariosAtivos = usuarioRepository.findByAtivo(true);

        log.info("Enviando resumo para {} usuários ativos", usuariosAtivos.size());

        if (usuariosAtivos.isEmpty()) {
            return;
        }

        // Busca promoções das últimas 24 horas
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);

        List<Passagem> todasPassagens = passagemRepository.findAll().stream()
                .filter(p -> p.getDataHoraConsulta().isAfter(ontem))
                .collect(Collectors.toList());

        log.info("Encontradas {} passagens nas últimas 24 horas", todasPassagens.size());

        // Filtra apenas as que são promoções
        List<Passagem> promocoes = todasPassagens.stream()
                .filter(passagemService::isPromocao)
                .sorted(Comparator.comparing(Passagem::getPreco))
                .collect(Collectors.toList());

        log.info("Encontradas {} promoções para incluir no resumo", promocoes.size());

        if (promocoes.isEmpty()) {
            log.info("Nenhuma promoção para enviar no resumo diário");
            return;
        }

        // Para cada usuário, envia o resumo das promoções
        for (Usuario usuario : usuariosAtivos) {
            enviarResumoPorUsuarioAsync(usuario.getId(), promocoes);
        }
    }

    /**
     * Envia o resumo de promoções para um usuário específico de forma assíncrona
     */
    @Async
    public void enviarResumoPorUsuarioAsync(Long usuarioId, List<Passagem> promocoes) {
        log.info("Enviando resumo assíncrono para usuário: {}", usuarioId);

        // Aqui poderia haver uma lógica para filtrar promoções relevantes
        // para este usuário específico com base em seus alertas

        // Para simplicidade, envia todas as promoções
        boolean enviado = notificacaoService.enviarResumoDiario(usuarioId, promocoes);

        if (enviado) {
            log.info("Resumo diário enviado com sucesso para usuário: {}", usuarioId);
        } else {
            log.error("Falha ao enviar resumo diário para usuário: {}", usuarioId);
        }
    }
}