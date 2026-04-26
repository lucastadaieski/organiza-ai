package com.organizaai.controller;

import com.organizaai.dto.EventoPublico;
import com.organizaai.model.Evento;
import com.organizaai.dto.EventoRequest;
import com.organizaai.dto.EventoResponse;
import com.organizaai.model.Usuario;
import com.organizaai.service.EventoService;
import com.organizaai.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestão de churrascos e outros eventos sociais")
public class EventoController {

    private final EventoService eventoService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<EventoResponse> criar(@RequestBody @Valid EventoRequest dto, Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Mapeamento manual (ou pode usar MapStruct no futuro)
        Evento evento = new Evento();
        evento.setNome(dto.nome());
        evento.setDescricao(dto.descricao());
        evento.setDataHora(dto.dataHora());
        evento.setLocalizacao(dto.localizacao());
        evento.setTipo(dto.tipo());
        evento.setOrganizador(logado);

        Evento salvo = eventoService.salvar(evento);

        // Transformando para o DTO de resposta
        EventoResponse response = new EventoResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getDescricao(),
                salvo.getDataHora(),
                salvo.getLocalizacao(),
                salvo.getTipo(),
                logado.getNome()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<Evento>> listarMeusEventos(Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(eventoService.listarPorOrganizador(logado.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid EventoRequest dto,
                                                    Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Convertendo DTO para Entity para processar no Service
        Evento dadosAtualizados = new Evento();
        dadosAtualizados.setNome(dto.nome());
        dadosAtualizados.setDescricao(dto.descricao());
        dadosAtualizados.setDataHora(dto.dataHora());
        dadosAtualizados.setLocalizacao(dto.localizacao());
        dadosAtualizados.setTipo(dto.tipo());

        Evento atualizado = eventoService.atualizar(id, dadosAtualizados, logado);

        return ResponseEntity.ok(mapearParaResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        eventoService.deletar(id, logado);
        return ResponseEntity.noContent().build(); // 204 No Content é o padrão para delete com sucesso
    }

    // Método auxiliar para evitar repetição de código (Refatoração)
    private EventoResponse mapearParaResponseDTO(Evento evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getNome(),
                evento.getDescricao(),
                evento.getDataHora(),
                evento.getLocalizacao(),
                evento.getTipo(),
                evento.getOrganizador().getNome()
        );
    }

    @GetMapping("/publico/{token}")
    public ResponseEntity<EventoPublico> buscarDadosPublicos(@PathVariable String token) {
        // Usamos o Service em vez do Repository diretamente
        Evento evento = eventoService.buscarPorToken(token);

        return ResponseEntity.ok(new EventoPublico(
                evento.getNome(),
                evento.getOrganizador().getNome(),
                evento.getDataHora(),
                evento.getLocalizacao()
        ));
    }
}
