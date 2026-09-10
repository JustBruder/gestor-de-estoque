package com.gestor.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "itens_avulsos")
public class ItemAvulso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double preco;
    private Double quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Getters e Setters mantidos normalmente...
}
