package com.gestor.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String pinSeguranca;

    public Usuario() {}

    public Usuario(Long id, String nome, String email, String senha, String pinSeguranca) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.pinSeguranca = pinSeguranca;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPinSeguranca() { return pinSeguranca; }
    public void setPinSeguranca(String pinSeguranca) { this.pinSeguranca = pinSeguranca; }
}