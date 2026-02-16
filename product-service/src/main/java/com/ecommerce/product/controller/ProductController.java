package com.ecommerce.product.controller;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stockQuantity) {
        log.info("Creating product: {}", name);
        Product product = productService.createProduct(name, description, price, stockQuantity);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable String productId) {
        log.info("Fetching product: {}", productId);
        Optional<Product> product = productService.getProduct(productId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String productId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stockQuantity) {
        log.info("Updating product: {}", productId);
        Product updatedProduct = productService.updateProduct(productId, name, description, price, stockQuantity);
        return updatedProduct != null ? 
                ResponseEntity.ok(updatedProduct) : 
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<String> decreaseStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        log.info("Decreasing stock for product: {}, quantity: {}", productId, quantity);
        boolean success = productService.decreaseStock(productId, quantity);
        return success ? 
                ResponseEntity.ok("Stock decreased successfully") : 
                ResponseEntity.badRequest().body("Insufficient stock or product not found");
    }
}
