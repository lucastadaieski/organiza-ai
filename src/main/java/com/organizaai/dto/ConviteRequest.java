package com.organizaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConviteRequest(
        @NotNull Long eventoId,
        @NotBlank String emailConvidado,
        String sugestaoContribuicao
) {}
