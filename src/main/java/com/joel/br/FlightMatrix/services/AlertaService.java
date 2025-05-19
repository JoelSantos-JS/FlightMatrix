package com.joel.br.FlightMatrix.services;

import com.joel.br.FlightMatrix.exceptions.ResourceNotFoundException;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.NotificacaoEnviada;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.models.Usuario;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.repository.AlertaRepository;
import com.joel.br.FlightMatrix.repository.NotificacaoEnviadaRepository;
import com.joel.br.FlightMatrix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para gerenciamento de alertas de preço de passagens
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AeroportoRepository aeroportoRepository;
    private final NotificacaoEnviadaRepository notificacaoEnviadaRepository;
    private final PassagemService passagemService;
    private final NotificacaoService notificacaoService;
    private final DealDiscoveryService dealDiscoveryService;
    private final UsuarioHelper usuarioHelper;

    /**
     * Cria um novo alerta de preço
     */
    @Transactional
    public Alerta criarAlerta(Alerta alerta) {
        log.info("Criando alerta para rota {} -> {} com preço máximo {}", 
                alerta.getOrigem(), alerta.getDestino(), alerta.getPrecoMaximo());
        
        // Valida e associa objetos relacionados
        Usuario usuario = usuarioRepository.findById(alerta.getUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        alerta.setUsuario(usuario);
        
        if (alerta.getOrigem() != null) {
            Aeroporto origem = aeroportoRepository.findById(alerta.getOrigem().getCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));
            alerta.setOrigem(origem);
        }
        
        if (alerta.getDestino() != null) {
            Aeroporto destino = aeroportoRepository.findById(alerta.getDestino().getCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));
            alerta.setDestino(destino);
        }
        
        // Ativa o alerta por padrão
        alerta.setAtivo(true);

        return alertaRepository.save(alerta);
    }

    /**
     * Atualiza um alerta existente
     */
    @Transactional
    public Alerta atualizarAlerta(Alerta alerta) {
        log.info("Atualizando alerta ID {}", alerta.getId());
        
        // Verifica se o alerta existe
        Alerta alertaExistente = alertaRepository.findById(alerta.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
        
        // Mantém o usuário original, apenas atualiza se informado
        if (alerta.getUsuario() != null && alerta.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(alerta.getUsuario().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            alertaExistente.setUsuario(usuario);
        }
        
        // Atualiza origem se informada
        if (alerta.getOrigem() != null && alerta.getOrigem().getCodigo() != null) {
            Aeroporto origem = aeroportoRepository.findById(alerta.getOrigem().getCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de origem não encontrado"));
            alertaExistente.setOrigem(origem);
        }
        
        // Atualiza destino se informado
        if (alerta.getDestino() != null && alerta.getDestino().getCodigo() != null) {
            Aeroporto destino = aeroportoRepository.findById(alerta.getDestino().getCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Aeroporto de destino não encontrado"));
            alertaExistente.setDestino(destino);
        }
        
        // Atualiza datas
        if (alerta.getDataIdaMinima() != null) {
            alertaExistente.setDataIdaMinima(alerta.getDataIdaMinima());
        }
        if (alerta.getDataIdaMaxima() != null) {
            alertaExistente.setDataIdaMaxima(alerta.getDataIdaMaxima());
        }
        if (alerta.getDataVoltaMinima() != null) {
            alertaExistente.setDataVoltaMinima(alerta.getDataVoltaMinima());
        }
        if (alerta.getDataVoltaMaxima() != null) {
            alertaExistente.setDataVoltaMaxima(alerta.getDataVoltaMaxima());
        }
        
        // Atualiza configurações
        if (alerta.getPrecoMaximo() != null) {
            alertaExistente.setPrecoMaximo(alerta.getPrecoMaximo());
        }
        if (alerta.getTempoMinimoPermanencia() != null) {
            alertaExistente.setTempoMinimoPermanencia(alerta.getTempoMinimoPermanencia());
        }
        if (alerta.getTempoMaximoPermanencia() != null) {
            alertaExistente.setTempoMaximoPermanencia(alerta.getTempoMaximoPermanencia());
        }
        if (alerta.getEscalasMaximas() != null) {
            alertaExistente.setEscalasMaximas(alerta.getEscalasMaximas());
        }
        if (alerta.getCompanhiasAereas() != null) {
            alertaExistente.setCompanhiasAereas(alerta.getCompanhiasAereas());
        }
        
        return alertaRepository.save(alertaExistente);
    }

    /**
     * Busca um alerta pelo ID
     */
    public Alerta buscarAlertaPorId(Long id) {
        log.info("Buscando alerta ID {}", id);
        return alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
    }

    /**
     * Lista alertas por usuário
     */
    public List<Alerta> buscarAlertasPorUsuario(Long usuarioId) {
        log.info("Buscando alertas para usuário ID {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        return alertaRepository.findByUsuario(usuario);
    }

    /**
     * Remove um alerta
     */
    @Transactional
    public void removerAlerta(Long id) {
        log.info("Removendo alerta ID {}", id);
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
        
        // Remover notificações relacionadas
        notificacaoEnviadaRepository.deleteByAlerta(alerta);
        
        // Remover o alerta
        alertaRepository.delete(alerta);
    }

    /**
     * Altera o status de um alerta (ativo/inativo)
     */
    @Transactional
    public Alerta alterarStatusAlerta(Long id, boolean ativo) {
        log.info("Alterando status do alerta ID {} para {}", id, ativo ? "ativo" : "inativo");
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));

        alerta.setAtivo(ativo);
        
        return alertaRepository.save(alerta);
    }

    /**
     * Lista as notificações enviadas para um alerta
     */
    public List<NotificacaoEnviada> buscarNotificacoesPorAlerta(Long alertaId) {
        log.info("Buscando notificações para o alerta ID {}", alertaId);
        
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
        
        return notificacaoEnviadaRepository.findByAlerta(alerta);
    }

    /**
     * Verifica todos os alertas ativos no sistema
     * @return Número de notificações enviadas
     */
    @Transactional
    public int verificarTodosAlertas() {
        log.info("Iniciando verificação de todos os alertas ativos");
        
        List<Alerta> alertasAtivos = alertaRepository.findByAtivo(true);
        log.info("Encontrados {} alertas ativos para verificação", alertasAtivos.size());
        
        int totalNotificacoesEnviadas = 0;
        
        for (Alerta alerta : alertasAtivos) {
            try {
                totalNotificacoesEnviadas += verificarAlerta(alerta);
            } catch (Exception e) {
                log.error("Erro ao verificar alerta ID {}: {}", alerta.getId(), e.getMessage(), e);
                // Continua verificando os demais alertas mesmo que um falhe
            }
        }
        
        log.info("Verificação de alertas concluída. Total de notificações enviadas: {}", totalNotificacoesEnviadas);
        return totalNotificacoesEnviadas;
    }

    /**
     * Verifica os alertas de um usuário específico
     * @param usuarioId ID do usuário
     * @return Número de notificações enviadas
     */
    @Transactional
    public int verificarAlertasUsuario(Long usuarioId) {
        log.info("Verificando alertas para usuário ID {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        List<Alerta> alertasUsuario = alertaRepository.findByUsuarioAndAtivo(usuario, true);
        log.info("Encontrados {} alertas ativos para o usuário", alertasUsuario.size());
        
        int totalNotificacoesEnviadas = 0;
        
        for (Alerta alerta : alertasUsuario) {
            try {
                totalNotificacoesEnviadas += verificarAlerta(alerta);
            } catch (Exception e) {
                log.error("Erro ao verificar alerta ID {}: {}", alerta.getId(), e.getMessage(), e);
            }
        }
        
        return totalNotificacoesEnviadas;
    }

    /**
     * Verifica um alerta específico, buscando por passagens que atendam aos critérios
     * @param alerta Alerta a ser verificado
     * @return Número de notificações enviadas (0 ou 1)
     */
    @Transactional
    public int verificarAlerta(Alerta alerta) {
        log.info("Verificando alerta ID {} (Origem: {}, Destino: {})", 
                alerta.getId(), alerta.getOrigem().getCodigo(), alerta.getDestino().getCodigo());
        
        // Verifica se podemos enviar uma nova notificação (limite de frequência)
        if (!podeEnviarNotificacao(alerta)) {
            log.info("Notificação para alerta ID {} não enviada por limite de frequência", alerta.getId());
            return 0;
        }
        
        // Busca passagens que atendam aos critérios do alerta
        List<Passagem> passagensEncontradas = new ArrayList<>();
        
        // Determina se é busca de ida ou ida e volta
        if (alerta.getDataVoltaMinima() == null) {
            // Busca só de ida
            passagensEncontradas = passagemService.buscarPorPreco(
                    alerta.getOrigem().getCodigo(), 
                    alerta.getDestino().getCodigo(), 
                    alerta.getDataIdaMinima(), 
                    alerta.getDataIdaMaxima(), 
                    alerta.getPrecoMaximo().doubleValue()
            );
        } else {
            // Busca de ida e volta
            // Primeiro faz a busca flexível
            List<Passagem> todasPassagens = passagemService.buscasPassagensFlexiveis(
                    alerta.getOrigem().getCodigo(), 
                    alerta.getDestino().getCodigo(), 
                    alerta.getDataIdaMinima(), 
                    alerta.getDataIdaMaxima(),
                    alerta.getDataVoltaMinima(),
                    alerta.getDataVoltaMaxima()
            );
            
            // Filtra as que atendem ao preço máximo
            for (Passagem passagem : todasPassagens) {
                if (passagem.getPreco() != null && 
                        passagem.getPreco().compareTo(alerta.getPrecoMaximo()) <= 0) {
                    passagensEncontradas.add(passagem);
                }
            }
        }
        
        // Filtra por escalas, se definido
        if (alerta.getEscalasMaximas() != null) {
            passagensEncontradas.removeIf(p -> 
                    p.getEscalas() != null && p.getEscalas() > alerta.getEscalasMaximas());
        }
        
        // Filtra por companhias aéreas, se definido
        if (alerta.getCompanhiasAereas() != null && !alerta.getCompanhiasAereas().isBlank()) {
            String[] companhias = alerta.getCompanhiasAereas().split(",");
            passagensEncontradas.removeIf(p -> {
                if (p.getCompanhiaAerea() == null) return true;
                
                for (String companhia : companhias) {
                    if (p.getCompanhiaAerea().toUpperCase().contains(companhia.trim().toUpperCase())) {
                        return false; // Mantém a passagem se a companhia estiver na lista
                    }
                }
                return true; // Remove se não encontrou nenhuma companhia compatível
            });
        }
        
        // Identifica as melhores ofertas entre as passagens encontradas
        List<Passagem> ofertas = dealDiscoveryService.identificarMelhoresOfertas(passagensEncontradas);
        
        // Se encontrou ofertas, envia notificação
        if (!ofertas.isEmpty()) {
            log.info("Encontradas {} ofertas para o alerta ID {}", ofertas.size(), alerta.getId());
            
            // Envia notificação ao usuário
            boolean notificacaoEnviada = notificacaoService.enviarNotificacaoAlerta(alerta, ofertas);
            
            if (notificacaoEnviada) {
                // Registra notificação enviada
                NotificacaoEnviada notificacao = new NotificacaoEnviada();
                notificacao.setAlerta(alerta);
                notificacao.setDataHoraEnvio(LocalDateTime.now());
                notificacao.setQuantidadeOfertas(ofertas.size());
                notificacao.setPrecoMinimo(
                        ofertas.stream()
                                .map(Passagem::getPreco)
                                .min(BigDecimal::compareTo)
                                .orElse(null)
                );
                
                notificacaoEnviadaRepository.save(notificacao);
                
                // Atualiza data da última notificação no alerta
                alerta.setUltimaNotificacao(LocalDateTime.now());
                alertaRepository.save(alerta);
                
                return 1;
            }
        } else {
            log.info("Nenhuma oferta encontrada para o alerta ID {}", alerta.getId());
        }
        
        return 0;
    }

    /**
     * Verifica se é possível enviar uma nova notificação para o alerta,
     * considerando o limite de frequência (para evitar spam)
     */
    private boolean podeEnviarNotificacao(Alerta alerta) {
        // Se nunca enviou notificação, pode enviar
        if (alerta.getUltimaNotificacao() == null) {
            return true;
        }
        
        // Verifica há quanto tempo foi enviada a última notificação
        LocalDateTime agora = LocalDateTime.now();
        long horasDesdeUltimaNotificacao = ChronoUnit.HOURS.between(alerta.getUltimaNotificacao(), agora);
        
        // Define um intervalo mínimo entre notificações (24 horas)
        final long INTERVALO_MINIMO_HORAS = 24;
        
        return horasDesdeUltimaNotificacao >= INTERVALO_MINIMO_HORAS;
    }

    /**
     * Retorna um usuário pelo seu ID
     */
    public Usuario getUsuarioById(Long usuarioId) {
        return usuarioHelper.getUsuarioById(usuarioId);
    }
}
