package com.gestor.estoque.security;

import com.gestor.estoque.model.Usuario;
import com.gestor.estoque.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Tenta extrair o email. Se no seu JwtUtil o nome for 'extractEmail', basta trocar a palavra abaixo.
                String email = jwtUtil.extractUsername(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

                    if (usuario != null) {
                        // RESOLVE O ERRO 2: Cria o UserDetails do jeito certo em vez de forçar a conversão.
                        // (Usei "" na senha para evitar erro caso sua entidade use outro nome de variável, o Spring não precisa dela aqui)
                        UserDetails userDetails = new User(usuario.getEmail(), "", new ArrayList<>());

                        // RESOLVE O ERRO 1: Passa o token e o userDetails juntos.
                        // ATENÇÃO: Se o seu método lá no JwtUtil se chamar 'isTokenValid', troque a palavra abaixo.
                        if (jwtUtil.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    email, null, userDetails.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Falha na validação do token JWT: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
