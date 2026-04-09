package com.organizaai.controller;

import com.organizaai.model.LoginRequest;
import com.organizaai.model.Usuario;
import com.organizaai.repository.UsuarioRepository;
import com.organizaai.service.AuthService;
import com.organizaai.service.JwtService;
import com.organizaai.service.MfaService;
import com.organizaai.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j // Para os logs de evidência
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final MfaService mfaService;
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        log.info("Tentativa de cadastro para o e-mail: {}", usuario.getEmail());

        // O seu UsuarioService precisa ter a lógica de encode que vimos antes
        usuarioService.cadastrar(usuario);

        log.info("Usuário cadastrado com sucesso e senha criptografada!");
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<String> setupMfa(@RequestParam String email) {
        // 1. Busca o usuário no banco
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Gera o segredo (Secret Key)
        String secret = mfaService.gerarSecret();

        // 3. Salva o segredo no usuário (mas ainda não ativa o MFA)
        usuario.setMfaSecret(secret);
        usuario.setMfaEnabled(false);
        usuarioRepository.save(usuario);

        // 4. Gera a URL para o QR Code e retorna para o Postman
        String qrCodeUrl = mfaService.gerarQrCodeUrl(secret, email);
        return ResponseEntity.ok(qrCodeUrl);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<String> verifyMfa(@RequestParam String email, @RequestParam int code) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Valida o código usando o MfaService
        boolean isCodeValid = mfaService.validarCodigo(usuario.getMfaSecret(), code);

        if (isCodeValid) {
            usuario.setMfaEnabled(true); // Ativa oficialmente o MFA para este usuário
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("MFA ativado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido ou expirado.");
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verificarLoginMfa(@RequestParam String email, @RequestParam String code) {
        // 1. Chama o service para validar o código e gerar o token
        // Se você seguiu o padrão de retornar um Map no service:
        try {
            var response = authService.verificarLoginMfa(email, code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido ou erro na autenticação.");
        }
    }
}
