package com.organizaai.model;

import com.organizaai.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "data_escolhida")
    private LocalDate dataEscolhida;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SugestaoData> opcoesData = new ArrayList<>();

    private String localizacao;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario organizador;

    //gera o id do link do convite
    @Column(unique = true)
    private String inviteToken = UUID.randomUUID().toString().substring(0, 8); // Token curto de 8 caracteres
}
