package com.organizaai.controller;

import com.organizaai.dto.EventoPublico;
import com.organizaai.model.Evento;
import com.organizaai.dto.EventoRequest;
import com.organizaai.dto.EventoResponse;
import com.organizaai.enums.TipoEvento;
import com.organizaai.model.SugestaoData;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        Evento evento = new Evento();
        evento.setNome(dto.titulo());
        evento.setDescricao(dto.descricao());
        evento.setLocalizacao(dto.localizacao());
        evento.setOrganizador(logado);

        // Converte a String do DTO para o Enum do Java (ex: "FESTA" -> TipoEvento.FESTA)
        if (dto.tipo() != null && !dto.tipo().isBlank()) {
            evento.setTipo(TipoEvento.valueOf(dto.tipo().toUpperCase()));
        }

        Evento salvo = eventoService.criarEventoComOpcoes(evento, dto.datas());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapearParaResponseDTO(salvo));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<EventoResponse>> listarMeusEventos(Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Mapeia a lista de Entidades para uma lista de DTOs para não expor a senha do organizador
        List<EventoResponse> respostas = eventoService.listarPorOrganizador(logado.getId())
                .stream()
                .map(this::mapearParaResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respostas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid EventoRequest dto,
                                                    Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Evento dadosAtualizados = new Evento();
        dadosAtualizados.setNome(dto.titulo());
        dadosAtualizados.setDescricao(dto.descricao());
        dadosAtualizados.setLocalizacao(dto.localizacao());

        if (dto.tipo() != null && !dto.tipo().isBlank()) {
            dadosAtualizados.setTipo(TipoEvento.valueOf(dto.tipo().toUpperCase()));
        }

        // Transforma a lista de LocalDate do DTO em lista de DataOpcao para o Service processar
        List<SugestaoData> novasOpcoes = new ArrayList<>();
        if (dto.datas() != null) {
            for (LocalDate data : dto.datas()) {
                SugestaoData op = new SugestaoData();
                op.setDataSugestao(data);
                novasOpcoes.add(op);
            }
        }
        dadosAtualizados.setOpcoesData(novasOpcoes);

        Evento atualizado = eventoService.atualizar(id, dadosAtualizados, logado);

        return ResponseEntity.ok(mapearParaResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Principal principal) {
        Usuario logado = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        eventoService.deletar(id, logado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/publico/{token}")
    public ResponseEntity<EventoPublico> buscarDadosPublicos(@PathVariable String token) {
        Evento evento = eventoService.buscarPorToken(token);

        // Puxa as datas de dentro dos objetos DataOpcao
        List<LocalDate> datas = evento.getOpcoesData().stream()
                .map(SugestaoData::getDataSugestao)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new EventoPublico(
                evento.getNome(),
                evento.getOrganizador().getNome(),
                datas,
                evento.getLocalizacao()
        ));
    }

    private EventoResponse mapearParaResponseDTO(Evento evento) {
        // Puxa as datas de dentro dos objetos DataOpcao
        List<LocalDate> datas = evento.getOpcoesData().stream()
                .map(SugestaoData::getDataSugestao)
                .collect(Collectors.toList());

        return new EventoResponse(
                evento.getId(),
                evento.getNome(),
                evento.getDescricao(),
                datas,
                evento.getLocalizacao(),
                evento.getTipo(),
                evento.getOrganizador().getNome(),
                evento.getInviteToken()
        );
    }
}