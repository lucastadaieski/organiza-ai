package com.organizaai.controller;

import com.organizaai.model.Usuario;
import com.organizaai.service.ParticipacaoService;
import com.organizaai.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/participacoes")
@RequiredArgsConstructor
public class ParticipacaoController {

    private final ParticipacaoService participacaoService;
    private final UsuarioService usuarioService;

    @PostMapping("/ingressar/{token}")
    public ResponseEntity<String> ingressar(@PathVariable String token, Principal principal) {
        // 1. Identificamos quem é o usuário pelo Token JWT
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Executamos a lógica de entrar no evento
        participacaoService.ingressarViaLink(token, logado);

        return ResponseEntity.ok("Sucesso! Você agora faz parte deste evento.");
    }
}
