package com.gestor.estoque.service;

import com.gestor.estoque.exception.EstoqueInsuficienteException;
import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendaService {

    private final ProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;

    public VendaService(ProdutoRepository produtoRepository, IngredienteRepository ingredienteRepository) {
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    @Transactional
    public void registrarVenda(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + produtoId));

        for (ItemReceita item : produto.getReceita()) {
            Ingrediente ingrediente = item.getIngrediente();
            double quantidadeNecessaria = item.getQuantidadeNecessaria();

            if (ingrediente.getQuantidadeEstoque() < quantidadeNecessaria) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente de '" + ingrediente.getNome() + "'. " +
                        "Necessário: " + quantidadeNecessaria + " " + ingrediente.getUnidadeMedida() + 
                        ", Disponível: " + ingrediente.getQuantidadeEstoque()
                );
            }
        }

        for (ItemReceita item : produto.getReceita()) {
            Ingrediente ingrediente = item.getIngrediente();
            double novoEstoque = ingrediente.getQuantidadeEstoque() - item.getQuantidadeNecessaria();
            ingrediente.setQuantidadeEstoque(novoEstoque);
            ingredienteRepository.save(ingrediente);
        }
    }
}