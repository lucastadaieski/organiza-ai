package com.organizaai.dto;

public record EventoPublico(
        String nome,
        String nomeOrganizador,
        java.time.LocalDate dataEvento,
        String localizacao
) {}
