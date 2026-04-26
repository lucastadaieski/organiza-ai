package com.organizaai.model;

import com.organizaai.enums.StatusParticipacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "participacoes")
@Data
@NoArgsConstructor
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private StatusParticipacao status = StatusParticipacao.PENDENTE;

    private String contribuicao; // Ex: "2kg de carne", "Refrigerante", "R$ 50,00"

    private LocalDateTime dataConvite = LocalDateTime.now();
}
