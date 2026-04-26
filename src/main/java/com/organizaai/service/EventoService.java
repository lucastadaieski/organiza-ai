package com.organizaai.service;

import com.organizaai.model.Evento;
import com.organizaai.model.Usuario;
import com.organizaai.repository.EventoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public Evento salvar(Evento evento) {
        // O Bean Validation já cuida da data futura,
        // mas aqui você pode adicionar outras regras de negócio no futuro.
        return eventoRepository.save(evento);
    }

    public List<Evento> listarPorOrganizador(Long usuarioId) {
        return eventoRepository.findByOrganizadorId(usuarioId);
    }

    @Transactional
    public Evento atualizar(Long id, Evento eventoAtualizado, Usuario organizador) {
        Evento eventoExistente = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Validação de Propriedade: O coração da segurança no CRUD
        if (!eventoExistente.getOrganizador().getId().equals(organizador.getId())) {
            throw new RuntimeException("Você não tem permissão para alterar este evento.");
        }

        // Atualizando os campos
        eventoExistente.setNome(eventoAtualizado.getNome());
        eventoExistente.setDescricao(eventoAtualizado.getDescricao());
        eventoExistente.setDataHora(eventoAtualizado.getDataHora());
        eventoExistente.setLocalizacao(eventoAtualizado.getLocalizacao());
        eventoExistente.setTipo(eventoAtualizado.getTipo());

        return eventoRepository.save(eventoExistente);
    }

    @Transactional
    public void deletar(Long id, Usuario organizador) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        if (!evento.getOrganizador().getId().equals(organizador.getId())) {
            throw new RuntimeException("Você não tem permissão para deletar este evento.");
        }

        eventoRepository.delete(evento);
    }

}
