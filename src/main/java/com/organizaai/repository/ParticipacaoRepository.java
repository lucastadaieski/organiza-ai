package com.organizaai.repository;

import com.organizaai.enums.StatusParticipacao;
import com.organizaai.model.Participacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {
    List<Participacao> findByEventoId(Long eventoId);
    List<Participacao> findByUsuarioIdAndStatus(Long usuarioId, StatusParticipacao status);
    Optional<Participacao> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
}
