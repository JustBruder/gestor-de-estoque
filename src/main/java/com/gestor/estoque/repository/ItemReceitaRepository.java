package com.gestor.estoque.repository;

import com.gestor.estoque.model.ItemReceita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReceitaRepository extends JpaRepository<ItemReceita, Long> {
}
