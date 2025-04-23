package com.joel.br.FlightMatrix.repository;

import com.joel.br.FlightMatrix.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByAtivo(Boolean ativo);
}
