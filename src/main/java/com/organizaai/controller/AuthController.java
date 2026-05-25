package com.organizaai.controller;

import com.organizaai.dto.LoginRequest;
import com.organizaai.dto.LoginResponse;
import com.organizaai.dto.RegisterRequest;
import com.organizaai.model.Usuario;
import com.organizaai.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final MfaService mfaService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetService passwordResetService;

    // --- ETAPA DE CADASTRO ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.registrar(request);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    // --- ETAPA DE CONFIGURAÇÃO MFA ---
    @GetMapping(value = "/mfa/setup", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> setupMfa(@RequestParam String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        byte[] qrCodeImage = mfaService.generateQrCodeImage(usuario.getMfaSecret(), email);
        return ResponseEntity.ok(qrCodeImage);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<String> verifyMfa(@RequestParam String email, @RequestParam String code) {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (mfaService.validarCodigo(usuario.getMfaSecret(), code)) {
            usuario.setMfaEnabled(true);
            usuarioService.atualizarUsuario(usuario);
            return ResponseEntity.ok("MFA ativado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido.");
    }

    // --- ETAPA DE LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.loginComSenha(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verificarLoginMfa(@RequestParam String email, @RequestParam String code) {
        try {
            var response = authService.verificarLoginMfa(email, code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido.");
        }
    }

    // --- ETAPA DE LOGOUT ---

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Date dataExpiracao = jwtService.extrairDataExpiracao(token);
        tokenBlacklistService.invalidarToken(token, dataExpiracao);
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            passwordResetService.gerarTokenRecuperacao(email);
            return ResponseEntity.ok("Se o e-mail existir em nossa base, você receberá um link de recuperação.");
        } catch (Exception e) {
            log.error("Erro ao solicitar recuperação de senha para {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao processar solicitação.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String novaSenha) {
        try {
            passwordResetService.redefinirSenha(token, novaSenha);
            return ResponseEntity.ok("Senha alterada com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}