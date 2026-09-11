package com.gestor.estoque.controller;

import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.ItemReceitaRepository;
import com.gestor.estoque.repository.ProdutoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private static final String ERRO_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    private static final String ERRO_PRODUTO_NAO_ENCONTRADO = "Produto não encontrado.";
    private static final String KEY_ERRO = "erro";
    private static final String KEY_MENSAGEM = "mensagem";

    private final ProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ItemReceitaRepository itemReceitaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoController(ProdutoRepository produtoRepository,
                             IngredienteRepository ingredienteRepository,
                             ItemReceitaRepository itemReceitaRepository,
                             UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.itemReceitaRepository = itemReceitaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<Produto>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));
        return ResponseEntity.ok(produtoRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Object> criar(@RequestBody Map<String, Object> body, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        if (!body.containsKey("nome") || !body.containsKey("preco")) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Nome e preço são obrigatórios."));
        }

        String nome = body.get("nome").toString().trim();
        Double preco = Double.valueOf(body.get("preco").toString());

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setUsuario(usuario);

        Produto produtoSalvo = produtoRepository.save(produto);

        if (body.containsKey("itensReceita")) {
            List<Map<String, Object>> itensRaw = (List<Map<String, Object>>) body.get("itensReceita");
            List<ItemReceita> receitaList = new ArrayList<>();

            for (Map<String, Object> itemMap : itensRaw) {
                Long ingId = Long.valueOf(itemMap.get("ingredienteId").toString());
                Double qtd = Double.valueOf(itemMap.get("quantidadeNecessaria").toString());

                Optional<Ingrediente> ingOpt = ingredienteRepository.findById(ingId);
                if (ingOpt.isPresent()) {
                    ItemReceita itemReceita = new ItemReceita();
                    itemReceita.setProduto(produtoSalvo);
                    itemReceita.setIngrediente(ingOpt.get());
                    itemReceita.setQuantidadeNecessaria(qtd);
                    itemReceitaRepository.save(itemReceita);
                    receitaList.add(itemReceita);
                }
            }
            produtoSalvo.setReceita(receitaList);
        }

        return ResponseEntity.ok(produtoSalvo);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        Optional<Produto> prodOpt = produtoRepository.findById(id);
        if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, ERRO_PRODUTO_NAO_ENCONTRADO));
        }

        Produto produto = prodOpt.get();
        if (produto.getReceita() != null && !produto.getReceita().isEmpty()) {
            itemReceitaRepository.deleteAll(produto.getReceita());
        }

        produtoRepository.delete(produto);
        return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Lanche excluído com sucesso!"));
    }
}
