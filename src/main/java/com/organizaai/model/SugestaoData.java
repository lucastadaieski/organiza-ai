package com.organizaai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sugestoes_data")
@Data
@NoArgsConstructor
public class SugestaoData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDate dataSugestao;

    // Relacionamento reverso: Uma sugestão tem vários votos.
    // O cascade = CascadeType.ALL garante que se a sugestão for deletada, os votos somem.
    @OneToMany(mappedBy = "sugestao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotoData> votos = new ArrayList<>();
}
