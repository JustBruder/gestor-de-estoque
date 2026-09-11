package com.gestor.estoque.controller;

import com.gestor.estoque.dto.ItemReceitaDTO;
import com.gestor.estoque.dto.ProdutoDTO;
import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.ItemReceita;
import com.gestor.estoque.model.Produto;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
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
    private final UsuarioRepository usuarioRepository;

    public ProdutoController(ProdutoRepository produtoRepository,
                             IngredienteRepository ingredienteRepository,
                             UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        // Agora o Spring já devolve o Produto com a lista de Receita montadinha pro Front-end!
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

        // Se veio ingredientes do app.js, a gente atrela eles no produto
        if (dto.getItensReceita() != null && !dto.getItensReceita().isEmpty()) {
            for (ItemReceitaDTO itemDto : dto.getItensReceita()) {
                Ingrediente ing = ingredienteRepository.findById(itemDto.getIngredienteId())
                        .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado"));

                ItemReceita itemReceita = new ItemReceita();
                itemReceita.setIngrediente(ing);
                itemReceita.setQuantidadeNecessaria(itemDto.getQuantidadeNecessaria());
                
                // Avisa de quem é essa receita e insere na lista do Produto
                itemReceita.setProduto(produto);
                produto.getReceita().add(itemReceita); 
            }
        }

        // Salvar o Produto agora salva as receitas junto (graças ao CascadeType.ALL)
        produtoRepository.save(produto);
        
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

        // Excluir o Produto agora apaga as receitas atreladas a ele no banco (graças ao orphanRemoval=true)
        produtoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Lanche excluído com sucesso!"));
    }
}
