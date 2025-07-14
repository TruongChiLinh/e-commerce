package com.example.dto;

import com.example.entity.User;
import java.util.Set;

public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private Set<User.Role> roles;

    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String username, String fullName, Set<User.Role> roles) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public Set<User.Role> getRoles() { return roles; }
    public void setRoles(Set<User.Role> roles) { this.roles = roles; }
}