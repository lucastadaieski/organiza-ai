package com.organizaai.service;

import com.organizaai.model.PasswordResetToken;
import com.organizaai.model.Usuario;
import com.organizaai.repository.PasswordResetTokenRepository;
import com.organizaai.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Injetando o nosso carteiro

    // 1. Gera o token e envia o e-mail real
    @Transactional // Precisa dessa anotação para fazer o delete e o insert na mesma transação
    public void gerarTokenRecuperacao(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // A MÁGICA ESTÁ AQUI: Limpa qualquer token antigo que ficou preso no banco
        tokenRepository.findByUsuario(usuario).ifPresent(tokenAntigo -> {
            tokenRepository.delete(tokenAntigo);
            tokenRepository.flush(); // Força o banco a apagar imediatamente antes de seguir
        });

        // Agora sim, gera e salva o novo em paz
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, usuario);
        tokenRepository.save(resetToken);

        // Dispara o e-mail
        emailService.enviarEmailRecuperacao(usuario.getEmail(), token);
    }

    // 2. Valida o token e troca a senha
    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Deleta o token para não ser usado duas vezes
        tokenRepository.delete(resetToken);
    }
}