package com.organizaai.repository;

import com.organizaai.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Busca todos os eventos onde o ID do organizador seja o fornecido
    List<Evento> findByOrganizadorId(Long usuarioId);

    Optional<Evento> findByInviteToken(String inviteToken);

}


