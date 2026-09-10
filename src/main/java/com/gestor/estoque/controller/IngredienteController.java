package com.gestor.estoque.controller;

import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ingredientes")
@CrossOrigin(origins = "*")
public class IngredienteController {

    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;

    public IngredienteController(IngredienteRepository ingredienteRepository, UsuarioRepository usuarioRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ingredienteRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            String nome = body.get("nome").toString().trim();

            if (ingredienteRepository.findByUsuarioIdAndNomeIgnoreCase(usuarioId, nome).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "O ingrediente '" + nome + "' já está cadastrado!"));
            }

            Double qtd = Double.valueOf(body.get("quantidadeEstoque").toString());
            String unidade = body.get("unidadeMedida").toString();

            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setNome(nome);
            ingrediente.setQuantidadeEstoque(qtd);
            ingrediente.setUnidadeMedida(unidade);
            ingrediente.setUsuario(usuario);

            return ResponseEntity.ok(ingredienteRepository.save(ingrediente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao salvar ingrediente: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        Optional<Ingrediente> ingOpt = ingredienteRepository.findById(id);
        if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ingrediente não encontrado."));
        }

        try {
            ingredienteRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensagem", "Ingrediente removido com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Este ingrediente está em uso numa receita ativa. Remova o lanche primeiro!"));
        }
    }

    @PostMapping("/{id}/acrescimo")
    public ResponseEntity<?> darBaixaAcrescimo(@PathVariable Long id, @RequestBody Map<String, Object> body, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        Optional<Ingrediente> ingOpt = ingredienteRepository.findById(id);
        if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ingrediente não encontrado."));
        }

        Double qtdAbater = Double.valueOf(body.getOrDefault("quantidade", 1.0).toString());
        Ingrediente ingrediente = ingOpt.get();

        if (ingrediente.getQuantidadeEstoque() < qtdAbater) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque insuficiente para esse acréscimo."));
        }

        ingrediente.setQuantidadeEstoque(ingrediente.getQuantidadeEstoque() - qtdAbater);
        ingredienteRepository.save(ingrediente);

        return ResponseEntity.ok(Map.of("mensagem", "Acréscimo registrado! Estoque de " + ingrediente.getNome() + " atualizado."));
    }
}