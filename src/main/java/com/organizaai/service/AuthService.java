package com.organizaai.service;

import com.organizaai.enums.Role;
import com.organizaai.model.LoginRequest;
import com.organizaai.model.RegisterRequest;
import com.organizaai.model.Usuario;
import com.organizaai.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public void registrar(RegisterRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        novoUsuario.setPassword(passwordEncoder.encode(request.senha()));

        novoUsuario.setRole(Role.USER);

        String secret = mfaService.gerarSecret();
        novoUsuario.setMfaSecret(secret);
        novoUsuario.setMfaEnabled(true);
        novoUsuario.setAtivo(true);

        repository.save(novoUsuario);
    }

    public Map<String, Object> login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Lógica do MFA que implementamos
        if (user.isMfaEnabled()) {
            return Map.of("mfaRequired", true);
        }

        // Se não tem MFA, gera o token
        String token = jwtService.gerarToken(user.getEmail());
        return Map.of("token", token, "mfaRequired", false);
    }

    public Map<String, Object> verificarLoginMfa(String email, String code) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // 2. Valida o código TOTP usando o segredo salvo no banco
        boolean isCodeValid = mfaService.verificarCodigo(user.getMfaSecret(), code);

        if (!isCodeValid) {
            throw new RuntimeException("Código MFA inválido");
        }

        // 3. Se o código estiver certo, gera o Token final
        String token = jwtService.gerarToken(user.getEmail());

        return Map.of(
                "token", token,
                "message", "Login realizado com sucesso!"
        );
    }

    public void processarFalhaLogin(String email) {
        Usuario usuario = repository.findByEmail(email).orElse(null);
        if (usuario != null) {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);

            if (usuario.getTentativasLogin() >= 5) {
                // Bloqueia por 15 minutos após 5 erros
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
                usuario.setTentativasLogin(0); // Reseta o contador para o próximo ciclo
            }
            repository.save(usuario);
        }
    }

    public void resetarTentativas(Usuario usuario) {
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        repository.save(usuario);
    }

}
