package com.devops.product.controller;
import com.devops.product.model.Product;
import com.devops.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductRepository repo;
    @GetMapping public ResponseEntity<List<Product>> all() { return ResponseEntity.ok(repo.findAll()); }
    @GetMapping("/{id}") public ResponseEntity<Product> one(@PathVariable Long id) { return ResponseEntity.ok(repo.findById(id).orElseThrow()); }
    @PostMapping public ResponseEntity<Product> create(@RequestBody Product p) { return ResponseEntity.ok(repo.save(p)); }
    @PutMapping("/{id}") public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product p) { p.setId(id); return ResponseEntity.ok(repo.save(p)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { repo.deleteById(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/health") public ResponseEntity<String> health() { return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"product-service\"}"); }
}
