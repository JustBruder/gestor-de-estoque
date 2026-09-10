package com.gestor.estoque.controller;

import com.gestor.estoque.dto.ItemAvulsoDTO;
import com.gestor.estoque.model.ItemAvulso;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.ItemAvulsoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/itens")
@CrossOrigin(origins = "https://conferindoestoque.vercel.app")
public class ItemAvulsoController {

    private static final String ERRO_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    private static final String ERRO_ITEM_NAO_ENCONTRADO = "Item não encontrado.";

    private final ItemAvulsoRepository itemRepository;
    private final UsuarioRepository usuarioRepository;

    public ItemAvulsoController(ItemAvulsoRepository itemRepository, UsuarioRepository usuarioRepository) {
        this.itemRepository = itemRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<ItemAvulso>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));
        return ResponseEntity.ok(itemRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody ItemAvulsoDTO dto, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        ItemAvulso item = new ItemAvulso();
        item.setNome(dto.nome().trim());
        item.setPreco(dto.preco());
        item.setQuantidadeEstoque(dto.quantidadeEstoque());
        item.setUsuario(usuario);

        ItemAvulso salvo = itemRepository.save(item);
        return ResponseEntity.ok(salvo);
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<Object> vender(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", ERRO_ITEM_NAO_ENCONTRADO));
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
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", ERRO_ITEM_NAO_ENCONTRADO));
        }

        itemRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Item excluído com sucesso!"));
    }
}
