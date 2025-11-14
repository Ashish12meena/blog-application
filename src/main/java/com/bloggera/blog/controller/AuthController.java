package com.bloggera.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.request.AuthenticationRequestDto;
import com.bloggera.blog.service.impl.AuthServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth/users")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<?> createUsers(@RequestBody AuthenticationRequestDto authCredentials){
        return authService.registerUser(authCredentials.getUsername(), authCredentials.getPassword());
        
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequestDto authCredentials) {
       return authService.loginUser(authCredentials.getUsername(),authCredentials.getPassword());
    }

    @GetMapping
    public String dolike(){
        return "Hello sir";
    }
}
