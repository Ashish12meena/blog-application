package com.bloggera.blog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.response.CategoriesResponseDto;
import com.bloggera.blog.service.impl.CategoryService;

@CrossOrigin(origins = "*", allowedHeaders = "Authorization")
@RestController
@RequestMapping("/api/category")
public class CategoryController {


    @Autowired
    private CategoryService categoryService;


    @PostMapping("/list")
    public List<CategoriesResponseDto> getListOfCategories(){
         return categoryService.getCategories();
    }

}
