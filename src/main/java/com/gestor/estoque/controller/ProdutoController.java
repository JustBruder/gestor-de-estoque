package com.gestor.estoque.controller;

import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.ProdutoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoController(ProdutoRepository produtoRepository, IngredienteRepository ingredienteRepository, UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(produtoRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            String nome = body.get("nome").toString().trim();
            Double preco = Double.valueOf(body.get("preco").toString());

            Produto produto = new Produto();
            produto.setNome(nome);
            produto.setPreco(preco);
            produto.setUsuario(usuario);

            List<Map<String, Object>> itens = (List<Map<String, Object>>) body.get("itensReceita");
            if (itens != null) {
                for (Map<String, Object> itemMap : itens) {
                    if (itemMap.get("ingredienteId") == null || itemMap.get("quantidadeNecessaria") == null) continue;

                    Long ingredienteId = Long.valueOf(itemMap.get("ingredienteId").toString());
                    Double qtd = Double.valueOf(itemMap.get("quantidadeNecessaria").toString());

                    Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId).orElse(null);
                    if (ingrediente != null) {
                        ItemReceita item = new ItemReceita();
                        item.setProduto(produto);
                        item.setIngrediente(ingrediente);
                        item.setQuantidadeNecessaria(qtd);
                        produto.getReceita().add(item);
                    }
                }
            }

            if (produto.getReceita().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Selecione pelo menos 1 ingrediente válido na receita."));
            }

            Produto salvo = produtoRepository.save(produto);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao processar receita: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        Optional<Produto> prodOpt = produtoRepository.findById(id);
        if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Lanche não encontrado."));
        }

        produtoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Lanche excluído com sucesso!"));
    }
}