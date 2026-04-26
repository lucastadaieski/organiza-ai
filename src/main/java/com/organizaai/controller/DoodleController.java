package com.organizaai.controller;

import com.organizaai.dto.DoodleResponse;
import com.organizaai.dto.FecharDoodleRequest;
import com.organizaai.dto.SugestaoDataRequest;
import com.organizaai.dto.VotoDataRequest;
import com.organizaai.service.DoodleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/eventos") // Aninhamos dentro da rota de eventos
@RequiredArgsConstructor
public class DoodleController {

    private final DoodleService doodleService;

    @PostMapping("/{eventoId}/sugestoes-data")
    public ResponseEntity<String> adicionarSugestoes(
            @PathVariable Long eventoId,
            @RequestBody @Valid SugestaoDataRequest request,
            Principal principal) {

        // O Spring injeta o email do usuário logado no objeto Principal
        String emailLogado = principal.getName();

        // Repassa a bola para o Service fazer o trabalho duro
        doodleService.adicionarSugestoes(eventoId, request, emailLogado);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Sugestões de datas adicionadas com sucesso!");
    }

    @PutMapping("/sugestoes/{sugestaoId}/votos")
    public ResponseEntity<String> votarData(
            @PathVariable Long sugestaoId,
            @RequestBody @Valid VotoDataRequest request,
            Principal principal) {

        doodleService.registrarVoto(sugestaoId, request, principal.getName());

        return ResponseEntity.ok("Voto registrado com sucesso!");
    }

    @GetMapping("/{eventoId}/doodle")
    public ResponseEntity<DoodleResponse> verApuracao(
            @PathVariable Long eventoId,
            Principal principal) {

        DoodleResponse response = doodleService.obterApuracao(eventoId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventoId}/fechar-doodle")
    public ResponseEntity<String> fecharDoodle(
            @PathVariable Long eventoId,
            @RequestBody @Valid FecharDoodleRequest request,
            Principal principal) {

        doodleService.fecharDoodle(eventoId, request, principal.getName());

        return ResponseEntity.ok("Votação encerrada! Data do evento definida com sucesso.");
    }

}
