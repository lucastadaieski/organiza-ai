package com.organizaai.dto;

public record VotoUsuarioDTO(
        Long usuarioId,
        String nome,
        Boolean disponivel
) {}
