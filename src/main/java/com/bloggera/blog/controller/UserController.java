package com.bloggera.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.user.UserDetailsRequest;
import com.bloggera.blog.model.User;
import com.bloggera.blog.service.impl.UserServiceImpl;

@CrossOrigin(origins = "*", allowedHeaders = "Authorization")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/userdetails")
    public ResponseEntity<?> getUserDetails(@RequestBody UserDetailsRequest request) {
        try {
            // Get the user by email first
            User targetUser = userService.getUserByEmail(request.getEmail());
            
            if (targetUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + request.getEmail());
            }

            // Now call getUserCardData with the correct user IDs
            // request.getUserId() is the logged-in user
            // targetUser.getId() is the user whose profile we're viewing
            return userService.getUserCardData(request.getEmail(), request.getUserId());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user details: " + e.getMessage());
        }
    }
}