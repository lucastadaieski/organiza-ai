package com.organizaai.controller;

import com.organizaai.model.LoginRequest;
import com.organizaai.model.RegisterRequest;
import com.organizaai.model.TokenBlacklist;
import com.organizaai.model.Usuario;
import com.organizaai.repository.TokenBlacklistRepository;
import com.organizaai.repository.UsuarioRepository;
import com.organizaai.service.AuthService;
import com.organizaai.service.JwtService;
import com.organizaai.service.MfaService;
import com.organizaai.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


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
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.isBloqueado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Conta bloqueada por excesso de tentativas. Tente novamente em alguns minutos.");
        }

        try {
            // Tenta autenticar a senha
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Se deu certo, reseta o contador
            authService.resetarTentativas(usuario);

            // Segue para o fluxo de MFA...
            return ResponseEntity.ok("Senha OK, prossiga para o MFA");

        } catch (BadCredentialsException e) {
            // Se errou a senha, registra a falha
            authService.processarFalhaLogin(request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha incorreta.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {


        // O seu UsuarioService precisa ter a lógica de encode que vimos antes
        authService.registrar(request);
        return ResponseEntity.ok("Usuário registrado!");
    }

    @GetMapping(value = "/mfa/setup", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Exibe o QR Code para configurar o 2FA no celular")
    public ResponseEntity<byte[]> setupMfa(@RequestParam String email) {
        // 1. Busca o usuário que já foi registrado
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Recupera o segredo que o método 'registrar' já criou
        String secret = usuario.getMfaSecret();

        // 3. Transforma o segredo e o e-mail em bytes de imagem PNG
        byte[] qrCodeImage = mfaService.generateQrCodeImage(secret, email);

        // 4. Retorna a imagem para o Swagger renderizar
        return ResponseEntity.ok(qrCodeImage);
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setToken(token);
        // Idealmente, pegue a data de expiração real do JWT para salvar aqui
        blacklist.setDataExpiracao(LocalDateTime.now().plusMinutes(15));

        tokenBlacklistRepository.save(blacklist);
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }
}
