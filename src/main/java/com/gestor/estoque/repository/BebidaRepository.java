package com.gestor.estoque.repository;

import com.gestor.estoque.model.Bebida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BebidaRepository extends JpaRepository<Bebida, Long> {
    List<Bebida> findByUsuarioId(Long usuarioId);
    Optional<Bebida> findByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}