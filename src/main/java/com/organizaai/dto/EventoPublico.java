package com.organizaai.dto;

import java.time.LocalDateTime;

public record EventoPublico(
        String nome,
        String nomeOrganizador,
        LocalDateTime dataHora,
        String localizacao
) {}
