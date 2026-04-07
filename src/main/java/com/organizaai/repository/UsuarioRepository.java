package com.organizaai.repository;

import com.organizaai.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Requisito de Segurança: Busca segura por email para autenticação
    Optional<Usuario> findByEmail(String email);
}
