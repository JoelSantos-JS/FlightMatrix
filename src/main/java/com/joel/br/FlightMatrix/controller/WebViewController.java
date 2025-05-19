package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.models.Passagem;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.services.DealDiscoveryService;
import com.joel.br.FlightMatrix.services.PassagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para servir páginas web utilizando Thymeleaf
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebViewController {

    private final PassagemService passagemService;
    private final DealDiscoveryService dealDiscoveryService;
    private final AeroportoRepository aeroportoRepository;
    
    /**
     * Página inicial
     */
    @GetMapping("/")
    public String home(Model model) {
        // Adiciona a lista de aeroportos para os selects
        List<Aeroporto> aeroportos = aeroportoRepository.findAll();
        model.addAttribute("aeroportos", aeroportos);
        
        // Adiciona datas padrão
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("amanha", LocalDate.now().plusDays(1));
        model.addAttribute("umMesDepois", LocalDate.now().plusMonths(1));
        
        // Adiciona flag para mostrar link para a interface direta
        model.addAttribute("showDirectApiLink", true);
        
        return "index";
    }
    
    /**
     * Página de resultados da busca
     */
    @GetMapping("/busca")
    public String buscarPassagens(
            @RequestParam String origem,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataIda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVolta,
            Model model) {
        
        log.info("Busca web: {} -> {}, ida: {}, volta: {}", origem, destino, dataIda, dataVolta);
        
        // Busca passagens
        List<Passagem> passagens = passagemService.buscarPassagens(origem, destino, dataIda, dataVolta);
        model.addAttribute("passagens", passagens);
        
        // Adiciona metadados da busca
        model.addAttribute("origem", origem);
        model.addAttribute("destino", destino);
        model.addAttribute("dataIda", dataIda);
        model.addAttribute("dataVolta", dataVolta);
        model.addAttribute("totalPassagens", passagens.size());
        
        return "resultados";
    }
    
    /**
     * Página de ofertas imperdíveis
     */
    @GetMapping("/ofertas")
    public String verOfertas(Model model) {
        log.info("Acessando página de ofertas");
        
        // Define um período razoável para buscar ofertas (próximo mês)
        LocalDate hoje = LocalDate.now();
        LocalDate umMesDepois = hoje.plusMonths(1);
        
        // Lista todos os aeroportos
        List<Aeroporto> aeroportos = aeroportoRepository.findAll();
        
        // Para simplicidade, considera apenas os 5 primeiros aeroportos como principais
        // Em um cenário real, usaríamos algum critério como volume de buscas
        List<Aeroporto> aeroportosPrincipais = aeroportos.stream()
                .filter(a -> a.getPrincipal() != null && a.getPrincipal())
                .limit(5)
                .toList();
        
        if (aeroportosPrincipais.size() < 2) {
            // Fallback caso não haja aeroportos marcados como principais
            aeroportosPrincipais = aeroportos.stream().limit(5).toList();
        }
        
        // Lista de passagens de oferta
        List<Passagem> ofertas = dealDiscoveryService.buscarMelhoresOfertas(
                aeroportosPrincipais.get(0).getCodigo(), 
                aeroportosPrincipais.get(1).getCodigo(), 
                hoje, 
                umMesDepois);
        
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("dataInicio", hoje);
        model.addAttribute("dataFim", umMesDepois);
        
        return "ofertas";
    }
} 