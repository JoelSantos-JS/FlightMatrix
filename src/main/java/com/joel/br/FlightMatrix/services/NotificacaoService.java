package com.joel.br.FlightMatrix.services;

import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.NotificacaoEnviada;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.models.Usuario;
import com.joel.br.FlightMatrix.repository.NotificacaoEnviadaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para envio de notificações por email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoService {

    private final NotificacaoEnviadaRepository notificacaoEnviadaRepository;
    private final EmailService emailService;
    private final UsuarioHelper usuarioHelper;
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String emailRemetente;
    
    @Value("${flightmatrix.notifications.enabled:true}")
    private boolean notificacoesHabilitadas;

    public boolean enviarNotificacaoAlerta(Alerta alerta, List<Passagem> ofertas) {
        if (!notificacoesHabilitadas) {
            log.info("Notificações desabilitadas. Não enviando email para o alerta ID {}", alerta.getId());
            return false;
        }
        
        try {
            Usuario usuario = alerta.getUsuario();
            if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                log.error("Usuário do alerta ID {} não possui email válido", alerta.getId());
                return false;
            }
            
            // Prepara o contexto para o template
            Context context = new Context(new Locale("pt", "BR"));
        context.setVariable("alerta", alerta);
            context.setVariable("ofertas", ofertas);
            context.setVariable("usuario", usuario);
            
            // Dados formatados para apresentação
            Map<String, String> formatados = prepararDadosFormatados(alerta, ofertas);
            context.setVariable("format", formatados);
            
            // Processa o template
            String emailContent = templateEngine.process("email-alerta", context);
            
            // Prepara e envia o email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(emailRemetente);
            helper.setTo(usuario.getEmail());
            helper.setSubject(String.format("Ofertas imperdíveis: %s → %s a partir de %s", 
                    alerta.getOrigem().getCodigo(), 
                    alerta.getDestino().getCodigo(), 
                    formatados.get("menorPreco")));
            helper.setText(emailContent, true); // true indica HTML
            
            mailSender.send(mimeMessage);
            
            log.info("Notificação enviada com sucesso para o alerta ID {} (usuário: {}, email: {})", 
                    alerta.getId(), usuario.getNome(), usuario.getEmail());
            
            return true;
        } catch (MessagingException e) {
            log.error("Erro ao enviar notificação por email para alerta ID {}: {}", 
                    alerta.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Prepara dados formatados para apresentação no template
     */
    private Map<String, String> prepararDadosFormatados(Alerta alerta, List<Passagem> ofertas) {
        Map<String, String> formatados = new HashMap<>();
        
        // Formata datas
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        if (alerta.getDataIdaMinima() != null) {
            formatados.put("dataIdaMinima", alerta.getDataIdaMinima().format(dateFormatter));
        }
        if (alerta.getDataIdaMaxima() != null) {
            formatados.put("dataIdaMaxima", alerta.getDataIdaMaxima().format(dateFormatter));
        }
        if (alerta.getDataVoltaMinima() != null) {
            formatados.put("dataVoltaMinima", alerta.getDataVoltaMinima().format(dateFormatter));
        }
        if (alerta.getDataVoltaMaxima() != null) {
            formatados.put("dataVoltaMaxima", alerta.getDataVoltaMaxima().format(dateFormatter));
        }
        
        // Encontra menor preço entre as ofertas
        BigDecimal menorPreco = ofertas.stream()
                .map(Passagem::getPreco)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        formatados.put("menorPreco", "R$ " + menorPreco.toString().replace(".", ","));
        formatados.put("precoMaximo", "R$ " + alerta.getPrecoMaximo().toString().replace(".", ","));
        
        // Outras informações úteis
        formatados.put("quantidadeOfertas", String.valueOf(ofertas.size()));
        
        return formatados;
    }
    
    /**
     * Envia notificação sobre resumo diário de ofertas
     * 
     * @param usuarioId ID do usuário destinatário
     * @param ofertas Lista de ofertas de passagens encontradas
     * @return true se a notificação foi enviada com sucesso, false caso contrário
     */
    public boolean enviarResumoDiario(Long usuarioId, List<Passagem> ofertas) {
        if (!notificacoesHabilitadas || ofertas.isEmpty()) {
            log.info("Notificações desabilitadas ou sem ofertas para o resumo diário. Usuário ID: {}", usuarioId);
            return false;
        }
        
        try {
            Usuario usuario = usuarioHelper.getUsuarioById(usuarioId);
            if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                log.error("Usuário ID {} não encontrado ou não possui email válido", usuarioId);
                return false;
            }
            
            // Prepara o contexto para o template
            Context context = new Context(new Locale("pt", "BR"));
            context.setVariable("usuario", usuario);
            context.setVariable("ofertas", ofertas);
            context.setVariable("dataAtual", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Prepara dados formatados para destinos mais populares
            Map<String, List<Passagem>> ofertasPorDestino = ofertas.stream()
                    .collect(Collectors.groupingBy(p -> p.getDestino().getCodigo()));
            
            List<Map<String, Object>> destinosPopulares = new ArrayList<>();
            ofertasPorDestino.forEach((destino, passagens) -> {
                Map<String, Object> destinoInfo = new HashMap<>();
                destinoInfo.put("codigo", destino);
                destinoInfo.put("cidade", passagens.get(0).getDestino().getCidade());
                
                BigDecimal menorPreco = passagens.stream()
                        .map(Passagem::getPreco)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                        
                destinoInfo.put("precoMinimo", "R$ " + menorPreco.toString().replace(".", ","));
                destinoInfo.put("quantidade", passagens.size());
                
                destinosPopulares.add(destinoInfo);
            });
            
            context.setVariable("destinos", destinosPopulares);
            
            // Processa o template
            String emailContent = templateEngine.process("email-resumo-diario", context);
            
            // Prepara e envia o email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(emailRemetente);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Resumo diário de ofertas de passagens - " + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM")));
            helper.setText(emailContent, true); // true indica HTML
            
            mailSender.send(mimeMessage);
            
            log.info("Resumo diário enviado com sucesso para o usuário ID {}", usuarioId);
            
            // Registra a notificação enviada
            for (Passagem oferta : ofertas.subList(0, Math.min(5, ofertas.size()))) {
                NotificacaoEnviada notificacao = new NotificacaoEnviada();
                notificacao.setPassagem(oferta);
                notificacao.setDataHoraEnvio(LocalDateTime.now());
                notificacao.setTipoNotificacao("RESUMO_DIARIO");
                notificacao.setSucesso(true);
                notificacaoEnviadaRepository.save(notificacao);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Erro ao enviar resumo diário para usuário ID {}: {}", 
                    usuarioId, e.getMessage(), e);
            return false;
        }
    }
}
