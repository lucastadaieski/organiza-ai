package com.organizaai.service;

import com.organizaai.enums.StatusParticipacao;
import com.organizaai.model.Evento;
import com.organizaai.model.Participacao;
import com.organizaai.model.Usuario;
import com.organizaai.repository.EventoRepository;
import com.organizaai.repository.ParticipacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipacaoService {

    private final ParticipacaoRepository participacaoRepository;
    private final EventoRepository eventoRepository;

    @Transactional
    public void ingressarViaLink(String token, Usuario usuario) {
        // 1. Localiza o evento pelo token do link
        Evento evento = eventoRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Este link de convite é inválido ou expirou."));

        // 2. Validação de Duplicidade: O usuário já está no evento?
        // Isso evita que o banco fique "sujo" com a mesma pessoa várias vezes
        boolean jaParticipa = participacaoRepository
                .findByEventoIdAndUsuarioId(evento.getId(), usuario.getId())
                .isPresent();

        if (jaParticipa) {
            throw new RuntimeException("Você já está participando deste evento!");
        }

        // 3. Cria a nova participação
        Participacao participacao = new Participacao();
        participacao.setEvento(evento);
        participacao.setUsuario(usuario);
        participacao.setStatus(StatusParticipacao.CONFIRMADO); // Entra direto como confirmado
        participacao.setContribuicao("A definir"); // Campo opcional que pode ser editado depois

        // 4. Salva no banco de dados
        participacaoRepository.save(participacao);
    }
}
