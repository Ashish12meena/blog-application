package com.bloggera.blog.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bloggera.blog.service.impl.ImageUploaderService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageUploaderService imageUploaderService;

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file){
        return ResponseEntity.ok(imageUploaderService.uploadImage(file));
    }

    @GetMapping 
    public List<String> getAllFiles(){
        return imageUploaderService.allFiles();
    }
}
