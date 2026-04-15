package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.model.RegisterRequest;
import com.organizaai.model.Usuario;
import lombok.RequiredArgsConstructor;
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

    //Registra um novo usuário no sistema.
    public void registrar(RegisterRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        novoUsuario.setPassword(passwordEncoder.encode(request.senha()));
        novoUsuario.setRole(Role.USER);
        novoUsuario.setAtivo(true);

        // Gera o segredo único do Google Authenticator para o usuário
        String secret = mfaService.gerarSecret();

        novoUsuario.setMfaSecret(secret);
        novoUsuario.setMfaEnabled(false);

        // Salva usando o UsuarioService
        usuarioService.atualizarUsuario(novoUsuario);
    }

    //Valida o código MFA (etapa 2 do login) e gera o JWT final.
    public Map<String, Object> verificarLoginMfa(String email, String code) {
        Usuario user = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Valida o código usando a String que chega do Controller
        boolean isCodeValid = mfaService.validarCodigo(user.getMfaSecret(), code);

        if (!isCodeValid) {
            throw new RuntimeException("Código MFA inválido ou expirado.");
        }

        // Se passou, gera o Token com validade de 15 minutos
        String token = jwtService.gerarToken(user.getEmail());

        return Map.of(
                "token", token,
                "message", "Login realizado com sucesso!"
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