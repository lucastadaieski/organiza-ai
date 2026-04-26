package com.organizaai.dto;

import java.util.List;

public record DoodleResponse(
        Long eventoId,
        String statusDoodle, // "ABERTO" ou "FECHADO"
        List<OpcaoDataDTO> opcoes
) {}
