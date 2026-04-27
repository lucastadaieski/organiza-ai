package com.organizaai.service;

import com.organizaai.dto.*;
import com.organizaai.model.Evento;
import com.organizaai.model.SugestaoData;
import com.organizaai.model.Usuario;
import com.organizaai.model.VotoData;
import com.organizaai.repository.EventoRepository;
import com.organizaai.repository.ParticipacaoRepository;
import com.organizaai.repository.SugestaoDataRepository;
import com.organizaai.repository.VotoDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.organizaai.dto.FecharDoodleRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoodleService {

    private final EventoRepository eventoRepository;
    private final SugestaoDataRepository sugestaoDataRepository;

    // Nossas 3 novas injeções para o sistema de votos:
    private final VotoDataRepository votoDataRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public void adicionarSugestoes(Long eventoId, SugestaoDataRequest request, String emailUsuarioLogado) {
        // 1. Busca o evento no banco
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));

        // 2. Trava de Segurança: Apenas o organizador pode criar sugestões de datas
        if (!evento.getOrganizador().getEmail().equals(emailUsuarioLogado)) {
            throw new RuntimeException("Acesso negado: Apenas o organizador do evento pode sugerir datas.");
        }

        // 3. Converte a lista de LocalDate (DTO) para a Entidade SugestaoData
        List<SugestaoData> entidadesParaSalvar = request.sugestoes().stream()
                .map(data -> {
                    SugestaoData sugestao = new SugestaoData();
                    sugestao.setEvento(evento);
                    sugestao.setDataSugestao(data);
                    return sugestao;
                })
                .collect(Collectors.toList());

        // 4. Salva todas as sugestões no banco de uma vez só (Performance!)
        sugestaoDataRepository.saveAll(entidadesParaSalvar);
    }

    @Transactional
    public void registrarVoto(Long sugestaoId, VotoDataRequest request, String emailUsuarioLogado) {
        // 1. Busca quem é o usuário votando
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 2. Busca qual é a data que ele está votando
        SugestaoData sugestao = sugestaoDataRepository.findById(sugestaoId)
                .orElseThrow(() -> new RuntimeException("Sugestão de data não encontrada."));

        // 3. Segurança: O usuário realmente foi convidado para este evento?
        // (Ou ele é o dono do evento, que também pode votar)
        Long eventoId = sugestao.getEvento().getId();
        boolean isOrganizador = sugestao.getEvento().getOrganizador().getId().equals(usuario.getId());
        boolean isParticipante = participacaoRepository.findByEventoIdAndUsuarioId(eventoId, usuario.getId()).isPresent();

        if (!isOrganizador && !isParticipante) {
            throw new RuntimeException("Acesso negado: Você não faz parte deste evento.");
        }

        // 4. Lógica de "Upsert": Busca o voto existente ou cria um novo em branco
        VotoData voto = votoDataRepository.findBySugestaoIdAndUsuarioId(sugestaoId, usuario.getId())
                .orElse(new VotoData());

        // 5. Aplica os valores e salva
        voto.setSugestao(sugestao);
        voto.setUsuario(usuario);
        voto.setDisponivel(request.disponivel());

        votoDataRepository.save(voto);
    }

    @Transactional(readOnly = true)
    public DoodleResponse obterApuracao(Long eventoId, String emailUsuarioLogado) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));

        // Busca todas as opções de data deste evento
        List<SugestaoData> sugestoes = sugestaoDataRepository.findByEventoId(eventoId);

        // Mapeia a lista de opções
        List<OpcaoDataDTO> opcoesDTO = sugestoes.stream().map(sugestao -> {

            // 1. Mapeia quem votou nesta sugestão
            List<VotoUsuarioDTO> votosDTO = sugestao.getVotos().stream()
                    .map(v -> new VotoUsuarioDTO(
                            v.getUsuario().getId(),
                            v.getUsuario().getNome(),
                            v.getDisponivel()))
                    .toList();

            // 2. Calcula o total de pessoas que disseram "Sim"
            long totalSim = sugestao.getVotos().stream()
                    .filter(VotoData::getDisponivel)
                    .count();

            // 3. Monta o DTO da Opção
            return new OpcaoDataDTO(sugestao.getId(), sugestao.getDataSugestao(), totalSim, votosDTO);

        }).toList();

        // Se a data do evento for null, a votação está aberta.
        String status = (evento.getDataEscolhida() == null) ? "ABERTO" : "FECHADO";

        return new DoodleResponse(evento.getId(), status, opcoesDTO);
    }
    @Transactional
    public void fecharDoodle(Long eventoId, FecharDoodleRequest request, String emailUsuarioLogado) {
        // 1. Busca o evento
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado."));

        // 2. Trava de Segurança: Só o dono do evento pode fechar a votação
        if (!evento.getOrganizador().getEmail().equals(emailUsuarioLogado)) {
            throw new RuntimeException("Acesso negado: Apenas o organizador pode definir a data final.");
        }

        // 3. Busca a sugestão escolhida
        SugestaoData sugestaoVencedora = sugestaoDataRepository.findById(request.sugestaoIdEscolhida())
                .orElseThrow(() -> new RuntimeException("Sugestão de data inválida."));

        // 4. Garante que a sugestão escolhida pertence a este evento
        if (!sugestaoVencedora.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("Esta sugestão de data não pertence a este evento.");
        }

        // 5. Bate o martelo: Define a data no evento principal
        evento.setDataEscolhida(sugestaoVencedora.getDataSugestao());
        eventoRepository.save(evento);
    }
}
