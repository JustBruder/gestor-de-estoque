package com.gestor.estoque.controller;

import com.gestor.estoque.model.Bebida;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.BebidaRepository;
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

import java.util.Map;

@RestController
@RequestMapping("/api/bebidas")
@CrossOrigin(origins = "https://conferindoestoque.vercel.app")
public class BebidaController {

    private static final String ERRO_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    private static final String ERRO_BEBIDA_NAO_ENCONTRADA = "Bebida não encontrada.";

    private final BebidaRepository bebidaRepository;
    private final UsuarioRepository usuarioRepository;

    public BebidaController(BebidaRepository bebidaRepository, UsuarioRepository usuarioRepository) {
        this.bebidaRepository = bebidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<Bebida>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));
        return ResponseEntity.ok(bebidaRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody Bebida bebida, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        bebida.setId(null); // Proteção contra Overposting
        bebida.setUsuario(usuario);
        Bebida salva = bebidaRepository.save(bebida);
        return ResponseEntity.ok(salva);
    }

    @PostMapping("/{id}/venda")
    public ResponseEntity<Object> vender(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", ERRO_BEBIDA_NAO_ENCONTRADA));
        }

        Bebida bebida = bebOpt.get();
        if (bebida.getQuantidadeEstoque() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Estoque insuficiente para esta bebida."));
        }

        bebida.setQuantidadeEstoque(bebida.getQuantidadeEstoque() - 1);
        bebidaRepository.save(bebida);
        return ResponseEntity.ok(Map.of("mensagem", "Venda de bebida registrada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var bebOpt = bebidaRepository.findById(id);
        if (bebOpt.isEmpty() || !bebOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", ERRO_BEBIDA_NAO_ENCONTRADA));
        }

        bebidaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensagem", "Bebida excluída com sucesso!"));
    }
}
