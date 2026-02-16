package com.ecommerce.product.service;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_EXPIRY = 1; // 1 hour

    public Product createProduct(String name, String description, double price, int stockQuantity) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID().toString());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getProductId());

        // Cache the product
        cacheProduct(savedProduct);

        return savedProduct;
    }

    public Optional<Product> getProduct(String productId) {
        // Try to get from cache first
        Product cachedProduct = getProductFromCache(productId);
        if (cachedProduct != null) {
            log.info("Product found in cache: {}", productId);
            return Optional.of(cachedProduct);
        }

        // Get from database
        Optional<Product> product = productRepository.findByProductId(productId);
        if (product.isPresent()) {
            cacheProduct(product.get());
        }

        return product;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(String productId, String name, String description, 
                                double price, int stockQuantity) {
        Optional<Product> productOpt = productRepository.findByProductId(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(product);
            cacheProduct(updatedProduct);
            log.info("Product updated: {}", productId);
            return updatedProduct;
        }
        return null;
    }

    public boolean decreaseStock(String productId, int quantity) {
        Optional<Product> productOpt = productRepository.findByProductId(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
                cacheProduct(product);
                log.info("Stock decreased for product: {}", productId);
                return true;
            }
        }
        return false;
    }

    private void cacheProduct(Product product) {
        try {
            redisTemplate.opsForValue().set(
                PRODUCT_CACHE_PREFIX + product.getProductId(),
                product,
                CACHE_EXPIRY,
                TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.warn("Failed to cache product: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Product getProductFromCache(String productId) {
        try {
            return (Product) redisTemplate.opsForValue().get(PRODUCT_CACHE_PREFIX + productId);
        } catch (Exception e) {
            log.warn("Failed to get product from cache: {}", e.getMessage());
            return null;
        }
    }
}
