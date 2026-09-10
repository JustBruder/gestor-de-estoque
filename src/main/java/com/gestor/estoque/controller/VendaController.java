package com.gestor.estoque.controller;

import com.gestor.estoque.exception.EstoqueInsuficienteException;
import com.gestor.estoque.service.VendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<?> vender(@PathVariable Long produtoId) {
        try {
            vendaService.registrarVenda(produtoId);
            return ResponseEntity.ok(Map.of("mensagem", "Venda realizada com sucesso! Estoque abatido."));
        } catch (EstoqueInsuficienteException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro ao processar venda: " + e.getMessage()));
        }
    }
}