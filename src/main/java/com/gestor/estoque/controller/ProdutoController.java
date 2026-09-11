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
import java.util.HashMap;
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
    private static final String KEY_NOME = "nome";
    private static final String KEY_PRECO = "preco";

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
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        List<Produto> produtos = produtoRepository.findByUsuarioId(usuario.getId());
        List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();
        List<Map<String, Object>> resposta = new ArrayList<>();

        for (Produto p : produtos) {
            Map<String, Object> prodMap = new HashMap<>();
            prodMap.put("id", p.getId());
            prodMap.put(KEY_NOME, p.getNome());
            prodMap.put(KEY_PRECO, p.getPreco());

            List<Map<String, Object>> receitaList = new ArrayList<>();
            for (ItemReceita item : todasReceitas) {
                if (item.getProduto() != null && item.getProduto().getId().equals(p.getId())) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("quantidadeNecessaria", item.getQuantidadeNecessaria());
                    itemMap.put("quantidade", item.getQuantidadeNecessaria());
                    if (item.getIngrediente() != null) {
                        Map<String, Object> ingMap = new HashMap<>();
                        ingMap.put("id", item.getIngrediente().getId());
                        ingMap.put(KEY_NOME, item.getIngrediente().getNome());
                        ingMap.put("unidadeMedida", item.getIngrediente().getUnidadeMedida());
                        itemMap.put("ingrediente", ingMap);
                        itemMap.put("ingredienteNome", item.getIngrediente().getNome());
                        itemMap.put("ingredienteId", item.getIngrediente().getId());
                    }
                    receitaList.add(itemMap);
                }
            }
            prodMap.put("receita", receitaList);
            prodMap.put("itensReceita", receitaList);
            resposta.add(prodMap);
        }

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> criar(@RequestBody Map<String, Object> body, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        if (!body.containsKey(KEY_NOME) || !body.containsKey(KEY_PRECO)) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Nome e preço são obrigatórios."));
        }

        String nome = body.get(KEY_NOME).toString().trim();
        Double preco = Double.valueOf(body.get(KEY_PRECO).toString());

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setUsuario(usuario);

        Produto produtoSalvo = produtoRepository.save(produto);

        Object itensObj = body.get("itensReceita");
        if (itensObj == null) {
            itensObj = body.get("receita");
        }

        if (itensObj instanceof List<?> itensList) {
            processarItensReceita(itensList, produtoSalvo);
        }

        return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Lanche cadastrado com sucesso!"));
    }

    private void processarItensReceita(List<?> itensList, Produto produtoSalvo) {
        for (Object itemObj : itensList) {
            if (itemObj instanceof Map<?, ?> itemMap) {
                Long ingId = extrairIngredienteId(itemMap);
                Double qtd = extrairQuantidade(itemMap);

                if (ingId != null && qtd != null) {
                    Optional<Ingrediente> ingOpt = ingredienteRepository.findById(ingId);
                    if (ingOpt.isPresent()) {
                        ItemReceita itemReceita = new ItemReceita();
                        itemReceita.setProduto(produtoSalvo);
                        itemReceita.setIngrediente(ingOpt.get());
                        itemReceita.setQuantidadeNecessaria(qtd);
                        itemReceitaRepository.save(itemReceita);
                    }
                }
            }
        }
    }

    private Long extrairIngredienteId(Map<?, ?> itemMap) {
        if (itemMap.containsKey("ingredienteId")) {
            return Long.valueOf(itemMap.get("ingredienteId").toString());
        }
        if (itemMap.containsKey("ingrediente_id")) {
            return Long.valueOf(itemMap.get("ingrediente_id").toString());
        }
        if (itemMap.containsKey("ingrediente") && itemMap.get("ingrediente") instanceof Map<?, ?> ingSubMap && ingSubMap.containsKey("id")) {
            return Long.valueOf(ingSubMap.get("id").toString());
        }
        if (itemMap.containsKey("id")) {
            return Long.valueOf(itemMap.get("id").toString());
        }
        return null;
    }

    private Double extrairQuantidade(Map<?, ?> itemMap) {
        if (itemMap.containsKey("quantidadeNecessaria")) {
            return Double.valueOf(itemMap.get("quantidadeNecessaria").toString());
        }
        if (itemMap.containsKey("quantidade")) {
            return Double.valueOf(itemMap.get("quantidade").toString());
        }
        if (itemMap.containsKey("qtd")) {
            return Double.valueOf(itemMap.get("qtd").toString());
        }
        return null;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        Optional<Produto> prodOpt = produtoRepository.findById(id);
        if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, ERRO_PRODUTO_NAO_ENCONTRADO));
        }

        Produto produto = prodOpt.get();
        List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();
        for (ItemReceita item : todasReceitas) {
            if (item.getProduto() != null && item.getProduto().getId().equals(produto.getId())) {
                itemReceitaRepository.delete(item);
            }
        }

        produtoRepository.delete(produto);
        return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Lanche excluído com sucesso!"));
    }
}
