package com.joel.br.FlightMatrix.services;


import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.NotificacaoEnviada;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.NotificacaoEnviadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificacaoService {

    private final NotificacaoEnviadaRepository notificacaoEnviadaRepository;
    private final EmailService emailService;
    private final AlertaService alertaService;
    private final TemplateEngine templateEngine;


    public  boolean enviarNotificaçaoPromocao(Alerta alerta, Passagem passagem) {

        log.info("Enviando notificação de promoção para usuário: {} (Alerta: {})",
                alerta.getUsuario().getEmail(), alerta.getId());


        Optional<NotificacaoEnviada> notificacaoExistente = notificacaoEnviadaRepository.findByAlertaAndPassagem(alerta, passagem);


        if(notificacaoExistente.isPresent()) {
            log.info("Notificação já enviada anteriormente");


            return  false;
        }

        Context context = new Context();
        context.setVariable("alerta", alerta);
        context.setVariable("passagem", passagem);
        context.setVariable("usuario", alerta.getUsuario());
        context.setVariable("dataEnvio", LocalDateTime.now());

        String conteudo = templateEngine.process("email-alerta", context);

        // Envia o email
        boolean sucesso = emailService.enviarEmail(
                alerta.getUsuario().getEmail(),
                "Alerta de Promoção: " + passagem.getOrigem().getCodigo() +
                        " -> " + passagem.getDestino().getCodigo(),
                conteudo
        );

        // Registra a notificação
        if (sucesso) {
            NotificacaoEnviada notificacao = new NotificacaoEnviada();
            notificacao.setAlerta(alerta);
            notificacao.setPassagem(passagem);
            notificacao.setTipoNotificacao("PROMOCAO");
            notificacao.setSucesso(true);
            notificacao.setConteudo(conteudo);

            notificacaoEnviadaRepository.save(notificacao);
            alertaService.registrarNotificacao(alerta);
        }

        return sucesso;
    }


    public  boolean enviarResumoDiario(Long usuarioId , List<Passagem> promocoes) {
        if(promocoes.isEmpty()) {


            return  false;
        }
        Map<String, List<Passagem>> promocoesPorRota = promocoes.stream()
                .collect(Collectors.groupingBy(p -> p.getOrigem().getCodigo() + "-" + p.getDestino().getCodigo()));

        // Prepara o conteúdo do email usando Thymeleaf
        Context context = new Context();
        context.setVariable("promocoesPorRota", promocoesPorRota);
        context.setVariable("totalPromocoes", promocoes.size());
        context.setVariable("dataEnvio", LocalDateTime.now());
        context.setVariable("dataReferencia", LocalDate.now());

        String conteudo = templateEngine.process("email-resumo-diario", context);

        // Busca alertas ativos do usuário para identificar os destinos de interesse
        List<Alerta> alertasUsuario = alertaService.buscarAlertasPorUsuario(usuarioId);
        String destinosInteresse = alertasUsuario.stream()
                .map(a -> a.getDestino().getCidade())
                .distinct()
                .limit(3)
                .collect(Collectors.joining(", "));

        // Envia o email
        return emailService.enviarEmail(
                alertasUsuario.get(0).getUsuario().getEmail(),
                "Resumo Diário de Promoções: " + destinosInteresse,
                conteudo
        );
    }

    public int processarPromocaoENotificar(Passagem passagem, boolean isPromocao) {
        if (!isPromocao) {
            return 0;
        }

        log.info("Processando promoção: {} -> {} (R$ {})",
                passagem.getOrigem().getCodigo(),
                passagem.getDestino().getCodigo(),
                passagem.getPreco());

        // Busca todos os alertas compatíveis com esta passagem
        List<Alerta> alertasCompativeis = alertaService.verificarAlertasCompativeis(passagem);

        log.info("Encontrados {} alertas compatíveis", alertasCompativeis.size());

        // Envia notificação para cada alerta
        int notificacoesEnviadas = 0;

        for (Alerta alerta : alertasCompativeis) {
            boolean enviado = enviarNotificaçaoPromocao(alerta, passagem);
            if (enviado) {
                notificacoesEnviadas++;
            }
        }

        return notificacoesEnviadas;
    }

}
