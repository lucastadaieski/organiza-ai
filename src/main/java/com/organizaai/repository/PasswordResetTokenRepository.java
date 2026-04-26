package com.organizaai.repository;

import com.organizaai.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Este método resolve o erro 'findByToken'
    Optional<PasswordResetToken> findByToken(String token);
}
