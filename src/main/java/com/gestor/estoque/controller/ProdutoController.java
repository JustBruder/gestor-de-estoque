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
    public ResponseEntity<List<Produto>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        return ResponseEntity.ok(produtoRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Object> criar(@RequestBody ProdutoDTO dto, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setPreco(dto.getPreco());
        produto.setUsuario(usuario);

        // PASSO 1: Força bruta. Salva o lanche primeiro pra ele existir de fato no banco.
        Produto produtoSalvo = produtoRepository.save(produto);

        // PASSO 2: Se tem ingredientes, vamos atrelar um por um.
        if (dto.getItensReceita() != null && !dto.getItensReceita().isEmpty()) {
            for (ItemReceitaDTO itemDto : dto.getItensReceita()) {
                Ingrediente ing = ingredienteRepository.findById(itemDto.getIngredienteId())
                        .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado"));

                ItemReceita itemReceita = new ItemReceita();
                itemReceita.setIngrediente(ing);
                itemReceita.setQuantidadeNecessaria(itemDto.getQuantidadeNecessaria());
                
                // Atrela ao lanche recém-salvo
                itemReceita.setProduto(produtoSalvo); 
                
                // PASSO 3: Salva a receita na marra. Chega de depender de mágica.
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

        produtoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Lanche excluído com sucesso!"));
    }
}
