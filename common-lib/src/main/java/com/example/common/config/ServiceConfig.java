package com.example.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceConfig {
    
    private String authServiceUrl = "http://localhost:8080";
    private String userServiceUrl = "http://localhost:8081";
    private String productServiceUrl = "http://localhost:8082";
    private String orderServiceUrl = "http://localhost:8083";
    private String paymentServiceUrl = "http://localhost:8085";

    // Getters and Setters
    public String getAuthServiceUrl() { return authServiceUrl; }
    public void setAuthServiceUrl(String authServiceUrl) { this.authServiceUrl = authServiceUrl; }
    
    public String getUserServiceUrl() { return userServiceUrl; }
    public void setUserServiceUrl(String userServiceUrl) { this.userServiceUrl = userServiceUrl; }
    
    public String getProductServiceUrl() { return productServiceUrl; }
    public void setProductServiceUrl(String productServiceUrl) { this.productServiceUrl = productServiceUrl; }
    
    public String getOrderServiceUrl() { return orderServiceUrl; }
    public void setOrderServiceUrl(String orderServiceUrl) { this.orderServiceUrl = orderServiceUrl; }
    
    public String getPaymentServiceUrl() { return paymentServiceUrl; }
    public void setPaymentServiceUrl(String paymentServiceUrl) { this.paymentServiceUrl = paymentServiceUrl; }
}