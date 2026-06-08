package com.devops.inventory.controller;

import com.devops.inventory.model.Product;
import com.devops.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ── UI ────────────────────────────────────────────────
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("products",       service.getAll());
        model.addAttribute("categories",     service.getCategories());
        model.addAttribute("newProduct",     new Product());
        model.addAttribute("totalProducts",  service.totalProducts());
        model.addAttribute("lowStock",       service.lowStockCount());
        model.addAttribute("outOfStock",     service.outOfStockCount());
        long inStock = service.totalProducts() - service.lowStockCount() - service.outOfStockCount();
        model.addAttribute("inStock", inStock);
        return "index";
    }

    @PostMapping("/products")
    public String create(@ModelAttribute Product product) {
        service.create(product);
        return "redirect:/";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }

    @GetMapping("/products/search")
    public String search(@RequestParam String q, Model model) {
        model.addAttribute("products",      service.search(q));
        model.addAttribute("categories",    service.getCategories());
        model.addAttribute("newProduct",    new Product());
        model.addAttribute("totalProducts", service.totalProducts());
        model.addAttribute("lowStock",      service.lowStockCount());
        model.addAttribute("outOfStock",    service.outOfStockCount());
        long inStock = service.totalProducts() - service.lowStockCount() - service.outOfStockCount();
        model.addAttribute("inStock", inStock);
        model.addAttribute("searchQuery",   q);
        return "index";
    }

    // ── REST API ──────────────────────────────────────────
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<Product>> apiAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> apiOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Product> apiCreate(@RequestBody Product p) {
        return ResponseEntity.ok(service.create(p));
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> apiUpdate(@PathVariable Long id, @RequestBody Product p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Void> apiDelete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"app\":\"inventory-manager\"}");
    }
}
