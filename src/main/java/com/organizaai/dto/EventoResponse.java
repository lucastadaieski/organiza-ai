package com.organizaai.dto;

import com.organizaai.enums.TipoEvento;
import java.time.LocalDateTime;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataHora,
        String localizacao,
        TipoEvento tipo,
        String nomeOrganizador
) {}
