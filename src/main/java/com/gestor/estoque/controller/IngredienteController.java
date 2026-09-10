package com.gestor.estoque.controller;

import com.gestor.estoque.model.ItemAvulso;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.ItemAvulsoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/itens")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemAvulsoRepository itemRepository;
    private final UsuarioRepository usuarioRepository;

    public ItemController(ItemAvulsoRepository itemRepository, UsuarioRepository usuarioRepository) {
        this.itemRepository = itemRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return ResponseEntity.ok(itemRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ItemAvulso item, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        item.setUsuario(usuario);
        return ResponseEntity.ok(itemRepository.save(item));
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<?> vender(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Item não encontrado."));
        }

        ItemAvulso item = itemOpt.get();
        if (item.getQuantidadeEstoque() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque insuficiente para este item."));
        }

        item.setQuantidadeEstoque(item.getQuantidadeEstoque() - 1);
        itemRepository.save(item);
        return ResponseEntity.ok(Map.of("mensagem", "Venda de item registrada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Item não encontrado."));
        }

        itemRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Item excluído com sucesso!"));
    }
}
