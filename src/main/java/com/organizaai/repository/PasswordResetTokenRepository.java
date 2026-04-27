package com.organizaai.repository;

import com.organizaai.model.PasswordResetToken;
import com.organizaai.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    Optional<PasswordResetToken> findByToken(String token);
}
