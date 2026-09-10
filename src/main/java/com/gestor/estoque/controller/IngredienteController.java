package com.gestor.estoque.controller;

import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return ResponseEntity.ok(ingredienteRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            String nome = body.get("nome").toString().trim();
            Double quantidadeEstoque = Double.valueOf(body.get("quantidadeEstoque").toString());
            String unidadeMedida = body.get("unidadeMedida").toString().trim();

            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setNome(nome);
            ingrediente.setQuantidadeEstoque(quantidadeEstoque);
            ingrediente.setUnidadeMedida(unidadeMedida);
            ingrediente.setUsuario(usuario);

            Ingrediente salvo = ingredienteRepository.save(ingrediente);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao cadastrar ingrediente: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/acrescimo")
    public ResponseEntity<?> darBaixaAcrescimo(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            var ingOpt = ingredienteRepository.findById(id);
            if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Ingrediente não encontrado."));
            }

            Ingrediente ingrediente = ingOpt.get();
            Double quantidadeAbater = body.containsKey("quantidade") ? Double.valueOf(body.get("quantidade").toString()) : 1.0;

            if (ingrediente.getQuantidadeEstoque() < quantidadeAbater) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Estoque insuficiente para abate."));
            }

            ingrediente.setQuantidadeEstoque(ingrediente.getQuantidadeEstoque() - quantidadeAbater);
            ingredienteRepository.save(ingrediente);

            return ResponseEntity.ok(Map.of("mensagem", "Baixa de " + quantidadeAbater + " " + ingrediente.getUnidadeMedida() + " realizada!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao dar baixa em acréscimo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var ingOpt = ingredienteRepository.findById(id);
        if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ingrediente não encontrado."));
        }

        ingredienteRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Ingrediente excluído com sucesso!"));
    }
}
