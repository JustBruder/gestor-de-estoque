package com.gestor.estoque.controller;

import com.gestor.estoque.model.Bebida;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.BebidaRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bebidas")
@CrossOrigin(origins = "*")
public class BebidaController {

    private final BebidaRepository bebidaRepository;
    private final UsuarioRepository usuarioRepository;

    public BebidaController(BebidaRepository bebidaRepository, UsuarioRepository usuarioRepository) {
        this.bebidaRepository = bebidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return ResponseEntity.ok(bebidaRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Bebida bebida, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        bebida.setUsuario(usuario);
        return ResponseEntity.ok(bebidaRepository.save(bebida));
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<?> vender(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Bebida não encontrada."));
        }

        Bebida bebida = bebOpt.get();
        if (bebida.getQuantidadeEstoque() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque insuficiente para esta bebida."));
        }

        bebida.setQuantidadeEstoque(bebida.getQuantidadeEstoque() - 1);
        bebidaRepository.save(bebida);
        return ResponseEntity.ok(Map.of("mensagem", "Venda de bebida registrada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Bebida não encontrada."));
        }

        bebidaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Bebida excluída com sucesso!"));
    }
}
