package com.organizaai.dto;

import com.organizaai.enums.TipoEvento;
import java.time.LocalDate;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        LocalDate dataEvento,
        String localizacao,
        TipoEvento tipo,
        String nomeOrganizador,
        String inviteToken
) {}
