package com.gestor.estoque.controller;

import com.gestor.estoque.model.Ingrediente;
import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.IngredienteRepository;
import com.gestor.estoque.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/api/ingredientes")
@CrossOrigin(origins = "*")
public class IngredienteController {

    private static final String ERRO_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
    private static final String ERRO_INGREDIENTE_NAO_ENCONTRADO = "Ingrediente não encontrado.";
    private static final String KEY_ERRO = "erro";
    private static final String KEY_MENSAGEM = "mensagem";

    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;

    public IngredienteController(IngredienteRepository ingredienteRepository, UsuarioRepository usuarioRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<Ingrediente>> listar(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));
        return ResponseEntity.ok(ingredienteRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody Map<String, Object> body, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        if (!body.containsKey("nome") || !body.containsKey("quantidadeEstoque") || !body.containsKey("unidadeMedida")) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Dados incompletos para o ingrediente."));
        }

        String nome = body.get("nome").toString().trim();
        Double quantidadeEstoque = Double.valueOf(body.get("quantidadeEstoque").toString());
        String unidadeMedida = body.get("unidadeMedida").toString().trim();

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNome(nome);
        ingrediente.setQuantidadeEstoque(quantidadeEstoque);
        ingrediente.setUnidadeMedida(unidadeMedida);
        ingrediente.setUsuario(usuario);

        Ingrediente salvo = ingredienteRepository.save(ingrediente);
        return ResponseEntity.ok(salvo);
    }

    @PostMapping("/{id}/acrescimo")
    public ResponseEntity<Object> darBaixaAcrescimo(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var ingOpt = ingredienteRepository.findById(id);
        if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, ERRO_INGREDIENTE_NAO_ENCONTRADO));
        }

        Ingrediente ingrediente = ingOpt.get();
        Double quantidadeAbater = body.containsKey("quantidade") ? Double.valueOf(body.get("quantidade").toString()) : 1.0;

        if (ingrediente.getQuantidadeEstoque() < quantidadeAbater) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Estoque insuficiente para abate."));
        }

        ingrediente.setQuantidadeEstoque(ingrediente.getQuantidadeEstoque() - quantidadeAbater);
        ingredienteRepository.save(ingrediente);

        return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Baixa de " + quantidadeAbater + " " + ingrediente.getUnidadeMedida() + " realizada!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(ERRO_USUARIO_NAO_ENCONTRADO));

        var ingOpt = ingredienteRepository.findById(id);
        if (ingOpt.isEmpty() || !ingOpt.get().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, ERRO_INGREDIENTE_NAO_ENCONTRADO));
        }

        try {
            ingredienteRepository.deleteById(id);
            return ResponseEntity.ok(Map.of(KEY_MENSAGEM, "Ingrediente excluído com sucesso!"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERRO, "Este ingrediente não pode ser excluído porque faz parte da receita de um lanche cadastrado! Exclua o lanche primeiro."));
        }
    }
}
