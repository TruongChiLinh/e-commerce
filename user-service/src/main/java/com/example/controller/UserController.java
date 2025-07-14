package com.example.controller;

import com.example.common.response.ApiResponse;
import com.example.entity.UserProfile;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfile>>> getAllUsers() {
        List<UserProfile> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfile>> getUserById(@PathVariable Long id) {
        UserProfile user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getUserByUserId(@PathVariable Long userId) {
        UserProfile user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfile>> createUser(@Valid @RequestBody UserProfile user) {
        UserProfile createdUser = userService.createUser(user);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUser(@PathVariable Long id, @RequestBody UserProfile user) {
        UserProfile updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}