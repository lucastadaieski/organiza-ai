package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.dto.RegisterRequest;
import com.organizaai.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.organizaai.dto.LoginResponse;

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
    private final AuthenticationManager authenticationManager;

    // --- 1. O NASCIMENTO  ---
    public void registrar(RegisterRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        // Senha criptografada: pilar 01 da segurança
        novoUsuario.setPassword(passwordEncoder.encode(request.senha()));
        novoUsuario.setRole(Role.USER);
        novoUsuario.setAtivo(true);

        novoUsuario.setConsentimentoAceito(request.consentimentoAceito());
        novoUsuario.setConsentimentoData(LocalDateTime.now());
        novoUsuario.setConsentimentoVersao("v1.0_2026");

        // Gera o segredo, mas deixa DESATIVADO até o primeiro login
        String secret = mfaService.gerarSecret();
        novoUsuario.setMfaSecret(secret);
        novoUsuario.setMfaEnabled(false);

        usuarioService.atualizarUsuario(novoUsuario);
    }

    // ...existing code...

    // Nova API: centraliza a lógica de login com senha no Service
    @Transactional
    public LoginResponse loginComSenha(String email, String senha) {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.isBloqueado()) {
            throw new RuntimeException("Conta bloqueada temporariamente.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            // sucesso: reseta tentativas e retorna o próximo passo do fluxo MFA
            resetarTentativas(usuario);

            return new LoginResponse(usuario.isMfaEnabled() ? "MFA_VERIFY" : "MFA_SETUP",
                    usuario.isMfaEnabled(),
                    "Senha correta.");

        } catch (BadCredentialsException e) {
            // registra falha e propaga para ser tratado pelo controller/handler
            processarFalhaLogin(email);
            throw e;
        }
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