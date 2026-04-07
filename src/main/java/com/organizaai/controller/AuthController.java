package com.organizaai.controller;

import com.organizaai.model.LoginRequest;
import com.organizaai.model.Usuario;
import com.organizaai.service.JwtService;
import com.organizaai.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Tentativa de login para o usuário: {}", request.email());

        // 1. Validar a senha com BCrypt (que já está no seu UsuarioService)
        boolean senhaValida = usuarioService.validarSenha(request.email(), request.password());

        if (senhaValida) {
            // 2. Se OK, gera o token de 15 minutos
            String token = jwtService.gerarToken(request.email());
            log.info("Login bem-sucedido! Token gerado para: {}", request.email());
            return ResponseEntity.ok(Map.of("token", token));
        }

        log.warn("Falha no login: Senha incorreta ou usuário inexistente.");
        return ResponseEntity.status(401).body("Credenciais inválidas");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        log.info("Tentativa de cadastro para o e-mail: {}", usuario.getEmail());

        // O seu UsuarioService precisa ter a lógica de encode que vimos antes
        usuarioService.cadastrar(usuario);

        log.info("Usuário cadastrado com sucesso e senha criptografada!");
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}
