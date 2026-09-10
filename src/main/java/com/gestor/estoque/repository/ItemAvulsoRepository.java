package com.gestor.estoque.repository;

import com.gestor.estoque.model.ItemAvulso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItemAvulsoRepository extends JpaRepository<ItemAvulso, Long> {
    List<ItemAvulso> findByUsuarioId(Long usuarioId);
    Optional<ItemAvulso> findByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}