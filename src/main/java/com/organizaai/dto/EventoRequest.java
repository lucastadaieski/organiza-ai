package com.organizaai.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public record EventoRequest(
        @NotBlank(message = "O título é obrigatório")
        String titulo,

        String descricao,

        @NotEmpty(message = "Pelo menos uma data deve ser sugerida")
        List<LocalDate> datas,

        String localizacao, // Pode ser opcional agora

        String tipo // Pode ser opcional agora
) {}
