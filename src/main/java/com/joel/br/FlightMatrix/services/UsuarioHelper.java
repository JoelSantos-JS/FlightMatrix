package com.joel.br.FlightMatrix.services;

import com.joel.br.FlightMatrix.exceptions.ResourceNotFoundException;
import com.joel.br.FlightMatrix.models.Usuario;
import com.joel.br.FlightMatrix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Componente auxiliar para operações com usuários
 * Criado para quebrar dependência circular entre AlertaService e NotificacaoService
 */
@Component
@RequiredArgsConstructor
public class UsuarioHelper {
    
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Retorna um usuário pelo seu ID
     */
    public Usuario getUsuarioById(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
    }
} 