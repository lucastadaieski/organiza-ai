package com.organizaai.dto;

public record LoginResponse(
        String proximoPasso,
        boolean mfaAtivo,
        String mensagem
) {}

