package com.organizaai.dto;

import com.organizaai.enums.TipoEvento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String descricao,

        @NotNull(message = "A data e hora são obrigatórias")
        @Future(message = "A data do evento deve ser no futuro")
        LocalDateTime dataHora,

        @NotBlank(message = "A localização é obrigatória")
        String localizacao,

        @NotNull(message = "O tipo de evento é obrigatório")
        TipoEvento tipo
) {}
