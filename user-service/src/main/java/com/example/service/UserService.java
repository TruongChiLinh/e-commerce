package com.example.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.entity.UserProfile;
import com.example.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserProfileRepository userRepository;

    public List<UserProfile> getAllUsers() {
        return userRepository.findAll();
    }

    public UserProfile getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserProfile getUserByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userId: " + userId));
    }

    public UserProfile createUser(UserProfile user) {
        logger.info("Creating user profile for userId: {}", user.getUserId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        UserProfile saved = userRepository.save(user);
        logger.info("User profile created with ID: {}", saved.getId());
        return saved;
    }

    public UserProfile updateUser(Long id, UserProfile userDetails) {
        UserProfile user = getUserById(id);
        
        user.setFullName(userDetails.getFullName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setAddress(userDetails.getAddress());
        user.setDateOfBirth(userDetails.getDateOfBirth());
        user.setAvatar(userDetails.getAvatar());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        UserProfile user = getUserById(id);
        userRepository.delete(user);
    }
}