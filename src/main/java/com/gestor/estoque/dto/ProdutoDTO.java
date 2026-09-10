package com.gestor.estoque.dto;

import java.util.List;

public class ProdutoDTO {
    private String nome;
    private Double preco;
    private List<ItemReceitaDTO> itensReceita;

    public ProdutoDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public List<ItemReceitaDTO> getItensReceita() { return itensReceita; }
    public void setItensReceita(List<ItemReceitaDTO> itensReceita) { this.itensReceita = itensReceita; }
}