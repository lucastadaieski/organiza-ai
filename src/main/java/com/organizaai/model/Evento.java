package com.organizaai.model;

import com.organizaai.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos") // O nome da tabela muda para 'eventos'
@Data
@NoArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private String localizacao;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipo; // Aqui entra o seu Enum

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario organizador;
}
