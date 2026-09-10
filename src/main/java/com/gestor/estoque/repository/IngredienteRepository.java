package com.gestor.estoque.repository;

import com.gestor.estoque.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    List<Ingrediente> findByUsuarioId(Long usuarioId);
    Optional<Ingrediente> findByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}