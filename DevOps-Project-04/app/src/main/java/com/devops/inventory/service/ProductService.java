package com.devops.inventory.service;

import com.devops.inventory.model.Product;
import com.devops.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    public List<Product> getAll()                        { return repo.findAll(); }
    public Product getById(Long id)                      { return repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found: " + id)); }
    public List<Product> getByCategory(String cat)       { return repo.findByCategory(cat); }
    public List<Product> search(String name)             { return repo.findByNameContainingIgnoreCase(name); }
    public List<String>  getCategories()                 { return repo.findAllCategories(); }

    public Product create(Product p) {
        updateStatus(p);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return repo.save(p);
    }

    public Product update(Long id, Product updated) {
        Product p = getById(id);
        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setCategory(updated.getCategory());
        p.setQuantity(updated.getQuantity());
        p.setPrice(updated.getPrice());
        p.setSku(updated.getSku());
        p.setUpdatedAt(LocalDateTime.now());
        updateStatus(p);
        return repo.save(p);
    }

    public void delete(Long id) { repo.deleteById(id); }

    // Stats for dashboard
    public long totalProducts()   { return repo.count(); }
    public long lowStockCount()   { return repo.countLowStock(); }
    public long outOfStockCount() { return repo.countOutOfStock(); }

    private void updateStatus(Product p) {
        if (p.getQuantity() == 0)       p.setStatus(Product.Status.OUT_OF_STOCK);
        else if (p.getQuantity() <= 10) p.setStatus(Product.Status.LOW_STOCK);
        else                            p.setStatus(Product.Status.IN_STOCK);
    }
}
