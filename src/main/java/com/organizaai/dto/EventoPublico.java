package com.organizaai.dto;

import java.time.LocalDate;
import java.util.List;

public record EventoPublico(
        String nome,
        String nomeOrganizador,
        List<LocalDate> datas,
        String localizacao
) {}
