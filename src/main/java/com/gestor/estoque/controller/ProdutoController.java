package com.gestor.estoque.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.ItemReceitaRepository;
import com.gestor.estoque.repository.ProdutoRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
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

import java.io.IOException;
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
    private static final String KEY_RECEITA = "receita";
    private static final String KEY_INGREDIENTES = "ingredientes";
    private static final String KEY_ITENS_RECEITA = "itensReceita";
    private static final String KEY_QTD_NECESSARIA = "quantidadeNecessaria";

    private final ProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ItemReceitaRepository itemReceitaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            Map<String, Object> prodMap = montarProdutoResponse(p, todasReceitas);
            resposta.add(prodMap);
        }

        return ResponseEntity.ok(resposta);
    }

    private Map<String, Object> montarProdutoResponse(Produto p, List<ItemReceita> todasReceitas) {
        Map<String, Object> prodMap = new HashMap<>();
        prodMap.put("id", p.getId());
        prodMap.put(KEY_NOME, p.getNome());
        prodMap.put(KEY_PRECO, p.getPreco());

        List<Map<String, Object>> receitaList = new ArrayList<>();
        List<String> receitaNomes = new ArrayList<>();
        for (ItemReceita item : todasReceitas) {
            if (item.getProduto() != null && item.getProduto().getId() != null && item.getProduto().getId().equals(p.getId())) {
                Map<String, Object> itemMap = montarItemReceitaMap(item);
                receitaList.add(itemMap);
                if (item.getIngrediente() != null) {
                    receitaNomes.add(item.getQuantidadeNecessaria() + " " + item.getIngrediente().getUnidadeMedida() + " de " + item.getIngrediente().getNome());
                }
            }
        }
        prodMap.put(KEY_RECEITA, receitaList);
        prodMap.put(KEY_ITENS_RECEITA, receitaList);
        prodMap.put(KEY_INGREDIENTES, receitaList);
        prodMap.put("receitaTexto", String.join(", ", receitaNomes));
        return prodMap;
    }

    private Map<String, Object> montarItemReceitaMap(ItemReceita item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", item.getId());
        itemMap.put(KEY_QTD_NECESSARIA, item.getQuantidadeNecessaria());
        itemMap.put("quantidade", item.getQuantidadeNecessaria());
        itemMap.put("qtd", item.getQuantidadeNecessaria());

        if (item.getIngrediente() != null) {
            Ingrediente ing = item.getIngrediente();
            Map<String, Object> ingMap = new HashMap<>();
            ingMap.put("id", ing.getId());
            ingMap.put(KEY_NOME, ing.getNome());
            ingMap.put("unidadeMedida", ing.getUnidadeMedida());

            itemMap.put("ingrediente", ingMap);
            itemMap.put(KEY_NOME, ing.getNome());
            itemMap.put("ingredienteNome", ing.getNome());
            itemMap.put("unidadeMedida", ing.getUnidadeMedida());
            itemMap.put("ingredienteId", ing.getId());
        }
        return itemMap;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> criar(@RequestBody Map<String, Object> body, Authentication authentication) {
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

        List<Ingrediente> todosIngredientes = ingredienteRepository.findAll();
        List<?> itensList = extrairListaDeReceita(body);
        processarItensReceita(itensList, produtoSalvo, todosIngredientes);

        List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();
        Map<String, Object> responseMap = montarProdutoResponse(produtoSalvo, todasReceitas);
        responseMap.put(KEY_MENSAGEM, "Lanche cadastrado com sucesso!");

        return ResponseEntity.ok(responseMap);
    }

    private List<?> extrairListaDeReceita(Map<String, Object> body) {
        String[] chaves = {KEY_ITENS_RECEITA, KEY_RECEITA, KEY_INGREDIENTES, "itens", "listaIngredientes", "ingredientesLanche", "itens_receita"};
        for (String chave : chaves) {
            Object val = body.get(chave);
            if (val != null) {
                if (val instanceof List<?> list && !list.isEmpty()) {
                    return list;
                } else if (val instanceof String strVal && !strVal.trim().isEmpty()) {
                    try {
                        return objectMapper.readValue(strVal, new TypeReference<List<Object>>() {});
                    } catch (IOException e) {
                        List<String> listFromCsv = List.of(strVal.split(","));
                        if (!listFromCsv.isEmpty()) {
                            return listFromCsv;
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private void processarItensReceita(List<?> itensList, Produto produtoSalvo, List<Ingrediente> todosIngredientes) {
        for (Object itemObj : itensList) {
            Ingrediente ing = buscarIngrediente(itemObj, todosIngredientes);
            Double qtd = extrairQuantidade(itemObj);

            if (ing != null) {
                ItemReceita itemReceita = new ItemReceita();
                itemReceita.setProduto(produtoSalvo);
                itemReceita.setIngrediente(ing);
                itemReceita.setQuantidadeNecessaria(qtd);
                itemReceitaRepository.save(itemReceita);
            }
        }
    }

    private Ingrediente buscarIngrediente(Object itemObj, List<Ingrediente> todosIngredientes) {
        if (itemObj == null) return null;

        String termoBusca = null;
        if (itemObj instanceof Map<?, ?> itemMap) {
            String[] chaves = {"ingredienteId", "ingrediente_id", "idIngrediente", "ingrediente", "id", "nome", "nomeIngrediente"};
            for (String chave : chaves) {
                if (itemMap.containsKey(chave) && itemMap.get(chave) != null) {
                    Object val = itemMap.get(chave);
                    if (val instanceof Map<?, ?> subMap) {
                        if (subMap.containsKey("id") && subMap.get("id") != null) {
                            termoBusca = subMap.get("id").toString();
                            break;
                        } else if (subMap.containsKey("nome") && subMap.get("nome") != null) {
                            termoBusca = subMap.get("nome").toString();
                            break;
                        }
                    } else {
                        termoBusca = val.toString();
                        break;
                    }
                }
            }
        } else {
            termoBusca = itemObj.toString();
        }

        if (termoBusca == null || termoBusca.trim().isEmpty()) return null;

        String busca = termoBusca.trim();

        try {
            Long id = Long.valueOf(busca);
            for (Ingrediente ing : todosIngredientes) {
                if (ing.getId() != null && ing.getId().equals(id)) {
                    return ing;
                }
            }
        } catch (NumberFormatException e) {
            // ignora se nao for numero
        }

        for (Ingrediente ing : todosIngredientes) {
            if (ing.getNome() != null && ing.getNome().equalsIgnoreCase(busca)) {
                return ing;
            }
        }

        for (Ingrediente ing : todosIngredientes) {
            if (ing.getNome() != null && (ing.getNome().toLowerCase().contains(busca.toLowerCase()) || busca.toLowerCase().contains(ing.getNome().toLowerCase()))) {
                return ing;
            }
        }

        return null;
    }

    private Double extrairQuantidade(Object itemObj) {
        if (itemObj instanceof Map<?, ?> itemMap) {
            String[] chavesQtd = {KEY_QTD_NECESSARIA, "quantidade", "qtd", "quantidade_necessaria", "qtdNecessaria"};
            for (String chave : chavesQtd) {
                if (itemMap.containsKey(chave) && itemMap.get(chave) != null) {
                    try {
                        return Double.valueOf(itemMap.get(chave).toString());
                    } catch (NumberFormatException e) {
                        // ignora
                    }
                }
            }
        }
        return 1.0;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> excluir(@PathVariable Long id, Authentication authentication) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

            Optional<Produto> prodOpt = produtoRepository.findById(id);
            if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, ERRO_PRODUTO_NAO_ENCONTRADO));
            }

            Produto produto = prodOpt.get();
            List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();
            for (ItemReceita item : todasReceitas) {
                if (item.getProduto() != null && item.getProduto().getId() != null && item.getProduto().getId().equals(produto.getId())) {
                    itemReceitaRepository.delete(item);
                }
            }

            produtoRepository.delete(produto);
            return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Lanche excluído com sucesso!"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Erro de integridade ao excluir lanche."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, e.getMessage()));
        }
    }
}
