package com.example.controller;

import com.example.common.response.ApiResponse;
import com.example.entity.UserProfile;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@Tag(name = "User Management", description = "User profile management operations")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all user profiles", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<UserProfile>>> getAllUsers() {
        List<UserProfile> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user profile by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfile>> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        UserProfile user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user by user ID", description = "Retrieve user profile by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserProfile> getUserByUserId(@Parameter(description = "User ID") @PathVariable Long userId) {
        UserProfile user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfile>> createUser(@Valid @RequestBody UserProfile user) {
        UserProfile createdUser = userService.createUser(user);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", createdUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserProfile> updateUser(@Parameter(description = "User ID") @PathVariable Long id, @RequestBody UserProfile user) {
        UserProfile updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}