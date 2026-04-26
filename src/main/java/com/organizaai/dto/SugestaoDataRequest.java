package com.organizaai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record SugestaoDataRequest(
        @NotEmpty(message = "Você precisa enviar pelo menos uma data.")
        @Size(max = 4, message = "O limite máximo é de 4 opções de datas.")
        List<LocalDate> sugestoes
) {}
