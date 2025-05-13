package com.joel.br.FlightMatrix.services;


import com.joel.br.FlightMatrix.exceptions.ResourceNotFoundException;
import com.joel.br.FlightMatrix.models.Alerta;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.models.Usuario;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.repository.AlertaRepository;
import com.joel.br.FlightMatrix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AeroportoRepository aeroportoRepository;







    public Alerta criarAlerta(Long usuarioId, Alerta alerta) {
        log.info("Criando Alerta para usuario: {}", usuarioId);


        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));

        alerta.setUsuario(usuario);
        alerta.setAtivo(true);


        return alertaRepository.save(alerta);
    }


    public Alerta atualizarAlerta(Long id, Alerta alertaAtualizado){


        Alerta alerta = alertaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
        if(alertaAtualizado.getOrigem() != null) {
            alerta.setOrigem(alertaAtualizado.getOrigem());
        }

        if(alertaAtualizado.getDestino() != null) {
            alerta.setDestino(alertaAtualizado.getDestino());
        }

        alerta.setDataIdaMinima(alertaAtualizado.getDataIdaMinima());
        alerta.setDataIdaMaxima(alertaAtualizado.getDataIdaMaxima());
        alerta.setDataVoltaMinima(alertaAtualizado.getDataVoltaMinima());
        alerta.setDataVoltaMaxima(alertaAtualizado.getDataVoltaMaxima());
        alerta.setPrecoMaximo(alertaAtualizado.getPrecoMaximo());
        alerta.setTempoMinimoPermanencia(alertaAtualizado.getTempoMinimoPermanencia());
        alerta.setTempoMaximoPermanencia(alertaAtualizado.getTempoMaximoPermanencia());
        alerta.setEscalasMaximas(alertaAtualizado.getEscalasMaximas());
        alerta.setCompanhiasAereas(alertaAtualizado.getCompanhiasAereas());
        alerta.setAtivo(alertaAtualizado.getAtivo());

        return alertaRepository.save(alerta);

    }


    public Alerta alterarStatusAlerta(Long id, boolean ativo) {
        Alerta alerta = alertaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));

        alerta.setAtivo(ativo);
        
        
        return alertaRepository.save(alerta);
    }
    public List<Alerta> buscarAlertasPorUsuario(Long usuarioId) {


        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));

        return  alertaRepository.findByUsuario(usuario);
    }


    public List<Alerta> verificarAlertasCompativeis(Passagem passagem) {

        List<Alerta> alertasPorRotas = alertaRepository.findAlertasCompativeis(
                passagem.getOrigem(),
                passagem.getDestino(),
                passagem.getDataIda(),
                passagem.getDataVolta()
        );


        return  alertasPorRotas.stream().filter(alerta -> vereficarPreco(alerta,passagem))
                .filter(alerta -> vereficarEscalas(alerta,passagem))
                .filter(alerta -> vereficarCompanhia(alerta,passagem))
                .filter(alerta -> vereficarTempoPermanencia(alerta,passagem))
                .collect(Collectors.toList());
    }

    private boolean vereficarCompanhia(Alerta alerta, Passagem passagem) {
        if (alerta.getCompanhiasAereas() == null || alerta.getCompanhiasAereas().trim().isEmpty()) {
            return true;
        }

        List<String> companhiasPreferidas = Arrays.asList(
                alerta.getCompanhiasAereas().split(","));

        return companhiasPreferidas.stream()
                .map(String::trim)
                .anyMatch(comp -> comp.equalsIgnoreCase(passagem.getCompanhiaAerea()));
    }

    private boolean vereficarPreco(Alerta alerta, Passagem passagem) {
        if(alerta.getPrecoMaximo() == null) {
            return  true;
        }

        return  passagem.getPreco().compareTo(alerta.getPrecoMaximo()) <= 0;
    }

    private boolean vereficarTempoPermanencia(Alerta alerta, Passagem passagem) {
        // Verifica tempo de permanência somente em passagens de ida e volta
        if (passagem.getDataVolta() == null) {
            return true;
        }

        int diasPermanencia = (int) ChronoUnit.DAYS.between(
                passagem.getDataIda(), passagem.getDataVolta());

        if (alerta.getTempoMinimoPermanencia() != null &&
                diasPermanencia < alerta.getTempoMinimoPermanencia()) {
            return false;
        }

        if (alerta.getTempoMaximoPermanencia() != null &&
                diasPermanencia > alerta.getTempoMaximoPermanencia()) {
            return false;
        }

        return true;

    }

    private boolean vereficarEscalas(Alerta alerta, Passagem passagem) {
        if(alerta.getEscalasMaximas() == null) {
            return true;
        }
        return  passagem.getEscalas() <= alerta.getEscalasMaximas();
    }



    public List<Alerta> buscarAlertasParaVereficar() {

        // Considera alertas que não foram notificados nas últimas 12 horas

        LocalDateTime  threshold = LocalDateTime.now().minusHours(12);

        return  alertaRepository.findAlertasParaVerificar(threshold);
    }

    
}
