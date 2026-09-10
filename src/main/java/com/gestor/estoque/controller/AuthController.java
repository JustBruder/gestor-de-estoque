package com.gestor.estoque.controller;

import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.UsuarioRepository;
import com.gestor.estoque.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, Object> body) {
        try {
            String email = body.get("email").toString().trim();
            String senhaBruta = body.get("senha").toString();
            String nome = body.get("nome").toString().trim();
            String pin = body.get("pinSeguranca").toString().trim();

            if (usuarioRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "E-mail já cadastrado!"));
            }

            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senhaBruta));
            usuario.setPinSeguranca(passwordEncoder.encode(pin));

            Usuario salvo = usuarioRepository.save(usuario);
            String token = jwtUtil.gerartoken(salvo.getEmail(), salvo.getId());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", salvo.getId(),
                    "nome", salvo.getNome(),
                    "email", salvo.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao cadastrar: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String email = body.get("email").toString().trim();
        String senhaBruta = body.get("senha").toString();

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isEmpty() || !passwordEncoder.matches(senhaBruta, userOpt.get().getSenha())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "E-mail ou senha inválidos."));
        }

        Usuario user = userOpt.get();
        String token = jwtUtil.gerartoken(user.getEmail(), user.getId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", user.getId(),
                "nome", user.getNome(),
                "email", user.getEmail()
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody Map<String, Object> body) {
        String email = body.get("email").toString().trim();
        String pinBruto = body.get("pinSeguranca").toString().trim();
        String novaSenha = body.get("novaSenha").toString();

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isEmpty() || !passwordEncoder.matches(pinBruto, userOpt.get().getPinSeguranca())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "E-mail ou PIN incorretos."));
        }

        Usuario usuario = userOpt.get();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso!"));
    }
}