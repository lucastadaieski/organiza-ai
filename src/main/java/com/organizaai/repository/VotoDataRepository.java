package com.organizaai.repository;

import com.organizaai.model.VotoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VotoDataRepository extends JpaRepository<VotoData, Long> {

    // Busca se um usuário específico já votou em uma sugestão específica
    Optional<VotoData> findBySugestaoIdAndUsuarioId(Long sugestaoId, Long usuarioId);
}
