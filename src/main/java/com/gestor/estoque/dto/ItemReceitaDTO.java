package com.gestor.estoque.dto;

public class ItemReceitaDTO {
    
    private Long ingredienteId;
    private Double quantidadeNecessaria;

    public ItemReceitaDTO() {}

    public Long getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(Long ingredienteId) { this.ingredienteId = ingredienteId; }
    
    public Double getQuantidadeNecessaria() { return quantidadeNecessaria; }
    public void setQuantidadeNecessaria(Double quantidadeNecessaria) { this.quantidadeNecessaria = quantidadeNecessaria; }
}
