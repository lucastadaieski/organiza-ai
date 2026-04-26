package com.organizaai.repository;

import com.organizaai.model.SugestaoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SugestaoDataRepository extends JpaRepository<SugestaoData, Long> {

    // Traz todas as datas sugeridas para um determinado evento
    List<SugestaoData> findByEventoId(Long eventoId);
}