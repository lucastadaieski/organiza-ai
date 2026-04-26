package com.organizaai.dto;

import jakarta.validation.constraints.NotNull;

public record FecharDoodleRequest(
        @NotNull(message = "Você precisa informar qual sugestão foi a escolhida.")
        Long sugestaoIdEscolhida
) {}