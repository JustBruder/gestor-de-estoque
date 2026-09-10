package com.gestor.estoque.controller;

import com.gestor.estoque.dto.ProdutoDTO;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.ProdutoRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "https://conferindoestoque.vercel.app")
public class ProdutoController {

    private static final String ERRO_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    private static final String ERRO_PRODUTO_NAO_ENCONTRADO = "Produto não encontrado.";

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IngredienteRepository ingredienteRepository;

    public ProdutoController(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, IngredienteRepository ingredienteRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<Produto>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));
        return ResponseEntity.ok(produtoRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody ProdutoDTO dto, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        Produto produto = new Produto();
        produto.setNome(dto.nome().trim());
        produto.setPreco(dto.preco());
        produto.setUsuario(usuario);

        if (dto.itensReceita() != null && !dto.itensReceita().isEmpty()) {
            List<ItemReceita> receita = new ArrayList<>();
            for (var itemDto : dto.itensReceita()) {
                var ingOpt = ingredienteRepository.findById(itemDto.ingredienteId());
                if (ingOpt.isPresent()) {
                    ItemReceita item = new ItemReceita();
                    item.setIngrediente(ingOpt.get());
                    item.setQuantidadeNecessaria(itemDto.quantidadeNecessaria());
                    item.setProduto(produto);
                    receita.add(item);
                }
            }
            produto.setReceita(receita);
        }

        Produto salvo = produtoRepository.save(produto);
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var prodOpt = produtoRepository.findById(id);
        if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", ERRO_PRODUTO_NAO_ENCONTRADO));
        }

        produtoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Produto excluído com sucesso!"));
    }
}
