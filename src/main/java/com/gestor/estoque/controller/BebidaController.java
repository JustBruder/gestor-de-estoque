package com.gestor.estoque.controller;

import com.gestor.estoque.model.Bebida;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.BebidaRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bebidas")
@CrossOrigin(origins = "*")
public class BebidaController {

    private final BebidaRepository bebidaRepository;
    private final UsuarioRepository usuarioRepository;

    public BebidaController(BebidaRepository bebidaRepository, UsuarioRepository usuarioRepository) {
        this.bebidaRepository = bebidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(bebidaRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            String nome = body.get("nome").toString().trim();

            if (bebidaRepository.findByUsuarioIdAndNomeIgnoreCase(usuarioId, nome).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "A bebida '" + nome + "' já está cadastrada!"));
            }

            Double preco = Double.valueOf(body.get("preco").toString());
            Double qtd = Double.valueOf(body.get("quantidadeEstoque").toString());

            Bebida bebida = new Bebida();
            bebida.setNome(nome);
            bebida.setPreco(preco);
            bebida.setQuantidadeEstoque(qtd);
            bebida.setUsuario(usuario);

            return ResponseEntity.ok(bebidaRepository.save(bebida));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao cadastrar bebida: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<?> vender(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        Optional<Bebida> bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Bebida não encontrada."));
        }

        Bebida bebida = bebOpt.get();
        if (bebida.getQuantidadeEstoque() < 1) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque esgotado para " + bebida.getNome()));
        }

        bebida.setQuantidadeEstoque(bebida.getQuantidadeEstoque() - 1);
        bebidaRepository.save(bebida);

        return ResponseEntity.ok(Map.of("mensagem", "Venda de " + bebida.getNome() + " registrada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        Optional<Bebida> bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuarioId)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Bebida não encontrada."));
        }

        bebidaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Bebida excluída com sucesso!"));
    }
}