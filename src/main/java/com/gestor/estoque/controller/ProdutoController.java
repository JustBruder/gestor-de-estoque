package com.gestor.estoque.controller;

import com.gestor.estoque.dto.ItemReceitaDTO;
import com.gestor.estoque.dto.ProdutoDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

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
    public ResponseEntity<List<Map<String, Object>>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // 1. Busca todos os lanches e TODAS as receitas na força bruta
        List<Produto> produtos = produtoRepository.findByUsuarioId(usuario.getId());
        List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();

        List<Map<String, Object>> resposta = new ArrayList<>();

        // 2. Monta o JSON perfeitamente formatado na mão para o app.js
        for (Produto p : produtos) {
            Map<String, Object> lanche = new HashMap<>();
            lanche.put("id", p.getId());
            lanche.put("nome", p.getNome());
            lanche.put("preco", p.getPreco());

            List<Map<String, Object>> receitaDoLanche = new ArrayList<>();

            for (ItemReceita item : todasReceitas) {
                if (item.getProduto() != null && item.getProduto().getId().equals(p.getId())) {
                    Map<String, Object> ingDetalhes = new HashMap<>();
                    ingDetalhes.put("nome", item.getIngrediente().getNome());

                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("quantidadeNecessaria", item.getQuantidadeNecessaria());
                    itemMap.put("ingrediente", ingDetalhes);

                    receitaDoLanche.add(itemMap);
                }
            }

            lanche.put("receita", receitaDoLanche);
            resposta.add(lanche);
        }

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Object> criar(@RequestBody ProdutoDTO dto, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // 1. Salva o lanche para garantir que ele existe
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setPreco(dto.getPreco());
        produto.setUsuario(usuario);
        Produto produtoSalvo = produtoRepository.save(produto);

        // 2. Salva cada item da receita individualmente 
        if (dto.getItensReceita() != null) {
            for (ItemReceitaDTO itemDto : dto.getItensReceita()) {
                Ingrediente ing = ingredienteRepository.findById(itemDto.getIngredienteId())
                        .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado"));

                ItemReceita itemReceita = new ItemReceita();
                itemReceita.setIngrediente(ing);
                itemReceita.setQuantidadeNecessaria(itemDto.getQuantidadeNecessaria());
                itemReceita.setProduto(produtoSalvo);

                itemReceitaRepository.save(itemReceita);
            }
        }

        return ResponseEntity.ok(Map.of("mensagem", "Lanche cadastrado com sucesso!"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var prodOpt = produtoRepository.findById(id);
        if (prodOpt.isEmpty() || !prodOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Produto não encontrado."));
        }

        // Apaga as receitas vinculadas primeiro na marra para não dar erro
        List<ItemReceita> todasReceitas = itemReceitaRepository.findAll();
        for (ItemReceita item : todasReceitas) {
            if (item.getProduto() != null && item.getProduto().getId().equals(id)) {
                itemReceitaRepository.delete(item);
            }
        }

        // Depois apaga o lanche
        produtoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Lanche excluído com sucesso!"));
    }
}
