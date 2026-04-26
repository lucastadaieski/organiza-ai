package com.organizaai.dto;

import java.time.LocalDate;
import java.util.List;

public record OpcaoDataDTO(
        Long sugestaoId,
        LocalDate data,
        long totalVotosSim,
        List<VotoUsuarioDTO> votosUsuarios
) {}
