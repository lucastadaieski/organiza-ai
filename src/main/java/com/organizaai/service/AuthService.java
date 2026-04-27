package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.dto.RegisterRequest;
import com.organizaai.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Serviço que orquestra as regras de negócio de Autenticação e Autorização.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;
    private final JwtService jwtService;

    // --- 1. O NASCIMENTO (Seu método original que está ótimo) ---
    public void registrar(RegisterRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        // Senha criptografada: pilar 01 da segurança
        novoUsuario.setPassword(passwordEncoder.encode(request.senha()));
        novoUsuario.setRole(Role.USER);
        novoUsuario.setAtivo(true);

        // Gera o segredo, mas deixa DESATIVADO até o primeiro login
        String secret = mfaService.gerarSecret();
        novoUsuario.setMfaSecret(secret);
        novoUsuario.setMfaEnabled(false);

        usuarioService.atualizarUsuario(novoUsuario);
    }

    // --- 2. O PRIMEIRO PASSO DO LOGIN (Validação de Senha) ---
    public Map<String, Object> autenticarSenha(String email, String senhaDigitada) {
        Usuario user = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Checa se a conta não está trancada por excesso de tentativas
        if (user.getBloqueadoAte() != null && user.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Conta bloqueada temporariamente.");
        }

        // BCrypt comparando a senha digitada com a do banco
        if (!passwordEncoder.matches(senhaDigitada, user.getPassword())) {
            processarFalhaLogin(email);
            throw new BadCredentialsException("Senha incorreta.");
        }

        resetarTentativas(user);

        // Retornamos o status para o Front-end saber para onde mandar o usuário
        return Map.of(
                "mfaAtivo", user.isMfaEnabled(),
                "status", user.isMfaEnabled() ? "MFA_VERIFY" : "MFA_SETUP"
        );
    }

    // --- 3. A CHAVE FINAL (Validação do Código + Entrega do JWT) ---
    public Map<String, Object> verificarLoginMfa(String email, String code) {
        Usuario user = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean isCodeValid = mfaService.validarCodigo(user.getMfaSecret(), code);

        if (!isCodeValid) {
            throw new RuntimeException("Código MFA inválido.");
        }

        // Se o usuário está vindo da tela de SETUP, agora a gente ativa o MFA de vez
        if (!user.isMfaEnabled()) {
            user.setMfaEnabled(true);
            usuarioService.atualizarUsuario(user);
        }

        // SÓ AQUI o Token é gerado!
        String token = jwtService.gerarToken(user.getEmail());

        return Map.of(
                "token", token,
                "nome", user.getNome(),
                "message", "Acesso liberado!"
        );
    }

    // Incrementa o contador de tentativas falhas e bloqueia a conta se necessário.
    public void processarFalhaLogin(String email) {
        // Usamos o ifPresent para evitar NullPointerException se tentarem logar com e-mail que não existe
        usuarioService.buscarPorEmail(email).ifPresent(usuario -> {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);

            if (usuario.getTentativasLogin() >= 5) {
                // Bloqueia a conta por 15 minutos
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
                usuario.setTentativasLogin(0); // Prepara o contador para o futuro
            }
            usuarioService.atualizarUsuario(usuario);
        });
    }

    //Reseta as falhas e remove o bloqueio quando o usuário acerta a senha.
    public void resetarTentativas(Usuario usuario) {
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        usuarioService.atualizarUsuario(usuario);
    }
}