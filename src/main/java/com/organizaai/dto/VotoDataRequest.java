package com.organizaai.dto;

import jakarta.validation.constraints.NotNull;

public record VotoDataRequest(
        @NotNull(message = "Você precisa informar se está disponível (true) ou não (false).")
        Boolean disponivel
) {}
