package com.gestor.estoque.controller;

import com.gestor.estoque.model.ItemAvulso;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.ItemAvulsoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/itens")
@CrossOrigin(origins = "*")
public class ItemAvulsoController {

    private final ItemAvulsoRepository itemAvulsoRepository;
    private final UsuarioRepository usuarioRepository;

    public ItemAvulsoController(ItemAvulsoRepository itemAvulsoRepository, UsuarioRepository usuarioRepository) {
        this.itemAvulsoRepository = itemAvulsoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        Long usuarioId = getUsuarioIdAutenticado();
        return ResponseEntity.ok(itemAvulsoRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = getUsuarioIdAutenticado();
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            String nome = body.get("nome").toString().trim();

            if (itemAvulsoRepository.findByUsuarioIdAndNomeIgnoreCase(usuarioId, nome).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "O item '" + nome + "' já está cadastrado!"));
            }

            Double preco = Double.valueOf(body.get("preco").toString());
            Double qtd = Double.valueOf(body.get("quantidadeEstoque").toString());

            ItemAvulso item = new ItemAvulso();
            item.setNome(nome);
            item.setPreco(preco);
            item.setQuantidadeEstoque(qtd);
            item.setUsuario(usuario);

            return ResponseEntity.ok(itemAvulsoRepository.save(item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao cadastrar item: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<?> vender(@PathVariable Long id) {
        Long usuarioId = getUsuarioIdAutenticado();
        Optional<ItemAvulso> itemOpt = itemAvulsoRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Item não encontrado."));
        }

        ItemAvulso item = itemOpt.get();
        if (item.getQuantidadeEstoque() < 1) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque esgotado para " + item.getNome()));
        }

        item.setQuantidadeEstoque(item.getQuantidadeEstoque() - 1);
        itemAvulsoRepository.save(item);

        return ResponseEntity.ok(Map.of("mensagem", "Venda de " + item.getNome() + " registrada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Long usuarioId = getUsuarioIdAutenticado();
        Optional<ItemAvulso> itemOpt = itemAvulsoRepository.findById(id);
        if (itemOpt.isEmpty() || !itemOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Item não encontrado."));
        }

        itemAvulsoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Item excluído com sucesso!"));
    }
}