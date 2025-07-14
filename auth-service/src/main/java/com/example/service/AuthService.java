package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.security.JwtUtil;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        logger.info("Registering user: {}", request.getUsername());
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists");
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        
        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.USER);
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }   

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException("Account is not active");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
        logger.info("User logged in successfully: {}", request.getUsername());
        
        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRoles());
    }

    public void logout(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("Invalid token");
        }
        logger.info("User logged out successfully");
    }

    public boolean validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.validateToken(token);
    }
}