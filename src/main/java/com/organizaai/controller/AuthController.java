package com.organizaai.controller;

import com.organizaai.dto.LoginRequest;
import com.organizaai.dto.RegisterRequest;
import com.organizaai.model.Usuario;
import com.organizaai.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Siga os passos numerados para testar o sistema de ponta a ponta")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final MfaService mfaService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetService passwordResetService;

    // --- ETAPA DE CADASTRO ---

    @Operation(summary = "01. Registrar", description = "Cria uma nova conta. O MFA iniciará desativado.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.registrar(request);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    // --- ETAPA DE CONFIGURAÇÃO MFA ---

    @Operation(summary = "02. Setup MFA", description = "Gera o QR Code. Escaneie-o com o Google Authenticator.")
    @GetMapping(value = "/mfa/setup", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> setupMfa(@RequestParam String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        byte[] qrCodeImage = mfaService.generateQrCodeImage(usuario.getMfaSecret(), email);
        return ResponseEntity.ok(qrCodeImage);
    }

    @Operation(summary = "03. Ativar MFA", description = "Valida o primeiro código para ativar o MFA na conta.")
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

    @Operation(summary = "04. Login - Senha", description = "Valida e-mail e senha. Se ok, pede o código MFA.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.buscarPorEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.isBloqueado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Conta bloqueada temporariamente."));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            authService.resetarTentativas(usuario);

            // MUDANÇA AQUI: Retornamos um JSON com o status do MFA
            return ResponseEntity.ok(Map.of(
                    "proximoPasso", "MFA_VERIFY",
                    "mfaAtivo", usuario.isMfaEnabled(),
                    "mensagem", "Senha correta."
            ));
        } catch (BadCredentialsException e) {
            authService.processarFalhaLogin(request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Senha incorreta."));
        }
    }

    @Operation(summary = "05. Login - Verificar MFA", description = "Valida o código do app e retorna o Token JWT final.")
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

    @Operation(summary = "06. Logout", description = "Invalida o token atual. Você deve estar autenticado (Authorize no topo).")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Date dataExpiracao = jwtService.extrairDataExpiracao(token);
        tokenBlacklistService.invalidarToken(token, dataExpiracao);
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }

    @Operation(summary = "07. Esqueci Senha", description = "Gera um token de recuperação e envia para o e-mail informado.")
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

    @Operation(summary = "08. Redefinir Senha", description = "Utiliza o token recebido no e-mail para cadastrar uma nova senha.")
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