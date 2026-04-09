package com.organizaai.service;

import com.organizaai.model.LoginRequest;
import com.organizaai.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MfaService mfaService;

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
}
