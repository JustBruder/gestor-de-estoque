package com.gestor.estoque.repository;

import com.gestor.estoque.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByUsuarioId(Long usuarioId);
}