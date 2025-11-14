package com.bloggera.blog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.user.UserDetailsRequest;
import com.bloggera.blog.service.impl.UserServiceImpl;

@CrossOrigin(origins = "*", allowedHeaders = "Authorization")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/userdetails")
    public ResponseEntity<?> getUserDetails(@RequestBody UserDetailsRequest request) {
        try {
            logger.info("Fetching user details for email: {}, requested by userId: {}", 
                request.getEmail(), request.getUserId());

            // Validate request
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                logger.error("Email is missing in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
            }

            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                logger.error("UserId is missing in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User ID is required");
            }

            // Call service with email and loggedInUserId
            ResponseEntity<?> response = userService.getUserCardData(
                request.getEmail(), 
                request.getUserId()
            );

            logger.info("Successfully fetched user details for email: {}", request.getEmail());
            return response;
            
        } catch (Exception e) {
            logger.error("Error fetching user details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user details: " + e.getMessage());
        }
    }
}