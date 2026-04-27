package com.organizaai.dto;

import com.organizaai.enums.TipoEvento;
import java.time.LocalDate;
import java.util.List;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        List<LocalDate> datas,
        String localizacao,
        TipoEvento tipo,
        String nomeOrganizador,
        String inviteToken
) {}
