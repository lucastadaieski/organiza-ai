package com.organizaai.service;

import com.organizaai.model.Evento;
import com.organizaai.model.SugestaoData;
import com.organizaai.model.Usuario;
import com.organizaai.repository.EventoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public Evento criarEventoComOpcoes(Evento evento, List<LocalDate> datasSugeridas) {

        for (LocalDate data : datasSugeridas) {
            SugestaoData sugestao = new SugestaoData(); // Usa a sua classe
            sugestao.setDataSugestao(data);             // Usa o campo da sua classe
            sugestao.setEvento(evento);

            evento.getOpcoesData().add(sugestao);
        }

        return eventoRepository.save(evento);
    }

    public List<Evento> listarPorOrganizador(Long usuarioId) {
        return eventoRepository.findByOrganizadorId(usuarioId);
    }

    @Transactional
    public Evento atualizar(Long id, Evento eventoAtualizado, Usuario organizador) {
        Evento eventoExistente = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Validação de Propriedade
        if (!eventoExistente.getOrganizador().getId().equals(organizador.getId())) {
            throw new RuntimeException("Você não tem permissão para alterar este evento.");
        }

        // Atualizando os campos de texto
        eventoExistente.setNome(eventoAtualizado.getNome());
        eventoExistente.setDescricao(eventoAtualizado.getDescricao());
        eventoExistente.setLocalizacao(eventoAtualizado.getLocalizacao());
        eventoExistente.setTipo(eventoAtualizado.getTipo());

        // MÁGICA AQUI: Atualizando as opções de data
        // 1. Limpamos as datas antigas
        eventoExistente.getOpcoesData().clear();

        // 2. Adicionamos as novas datas, garantindo a relação Pai e Filho
        if (eventoAtualizado.getOpcoesData() != null) {
            for (SugestaoData novaOpcao : eventoAtualizado.getOpcoesData()) {
                novaOpcao.setEvento(eventoExistente);
                eventoExistente.getOpcoesData().add(novaOpcao);
            }
        }

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

    public Evento buscarPorToken(String token) {
        return eventoRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Convite inválido ou expirado"));
    }

}
