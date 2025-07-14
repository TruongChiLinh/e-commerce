package com.example.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.entity.Product;
import com.example.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product createProduct(Product product) {
        logger.info("Creating product: {}", product.getName());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        logger.info("Product created with ID: {}", saved.getId());
        return saved;
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setCategory(productDetails.getCategory());
        product.setBrand(productDetails.getBrand());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStatus(productDetails.getStatus());
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public boolean updateStock(Long productId, Integer quantity) {
        try {
            Product product = getProductById(productId);
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void restoreStock(Long productId, Integer quantity) {
        try {
            Product product = getProductById(productId);
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);
        } catch (Exception e) {
            logger.error("Failed to restore stock for product: {}", productId, e);
        }
    }
}