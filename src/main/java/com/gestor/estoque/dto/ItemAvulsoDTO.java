package com.gestor.estoque.dto;

public record ItemAvulsoDTO(
    String nome,
    Double preco,
    Double quantidadeEstoque
) {}
