package com.gestor.estoque.dto;

public record IngredienteDTO(
    String nome,
    Double quantidadeEstoque,
    String unidadeMedida
) {}
