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
    public ResponseEntity<?> criar(@RequestBody Ingrediente ingrediente, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        ingrediente.setUsuario(usuario);
        return ResponseEntity.ok(ingredienteRepository.save(ingrediente));
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
