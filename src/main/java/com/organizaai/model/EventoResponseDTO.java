package com.organizaai.model;

import com.organizaai.enums.TipoEvento;
import java.time.LocalDateTime;

public record EventoResponseDTO(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataHora,
        String localizacao,
        TipoEvento tipo,
        String nomeOrganizador
) {}
