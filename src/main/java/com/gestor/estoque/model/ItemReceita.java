package com.gestor.estoque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "itens_receita")
public class ItemReceita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    @JsonIgnore
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @Column(nullable = false)
    private Double quantidadeNecessaria;

    public ItemReceita() {}

    public ItemReceita(Long id, Produto produto, Ingrediente ingrediente, Double quantidadeNecessaria) {
        this.id = id;
        this.produto = produto;
        this.ingrediente = ingrediente;
        this.quantidadeNecessaria = quantidadeNecessaria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public Double getQuantidadeNecessaria() { return quantidadeNecessaria; }
    public void setQuantidadeNecessaria(Double quantidadeNecessaria) { this.quantidadeNecessaria = quantidadeNecessaria; }
}