package com.bloggera.blog.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bloggera.blog.dto.response.Response;
import com.bloggera.blog.dto.response.UserDetailsResponse;
import com.bloggera.blog.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final UserServiceImpl userServiceImpl;
    private final AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public ResponseEntity<?> registerUser(String username, String password) {
        Map<String, Object> responseBody = new HashMap<>();
        if (userServiceImpl.existsByUsername(username)) {
            // Email already exists, return custom error response
            responseBody.put("message", new Response("error", "User already exists"));
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        }
        User user = new User();
        user.setPassword(encoder.encode(password));
        user.setUsername(username);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userServiceImpl.save(user);
        responseBody.put("message", new Response("ok", "User Register Successfully"));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    public ResponseEntity<?> loginUser(String username, String password) {
        UserDetailsResponse userDetails = new UserDetailsResponse();

        Map<String, Object> responseBody = new HashMap<>();
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authentication.isAuthenticated()) {
            User sendUser = userServiceImpl.getUserByUsername(username);
            userDetails.setEmail(sendUser.getEmail());
            userDetails.setUserId(sendUser.getId());
            userDetails.setUsername(sendUser.getUsername());
            userDetails.setProfilePicture(sendUser.getProfilePicture());
            responseBody.put("user", userDetails);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseBody);
        } else {

            responseBody.put("message", "User is not authenticated");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
    }

}
