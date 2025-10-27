package com.bloggera.blog.service.impl;



import java.util.List;
import org.springframework.stereotype.Service;

import com.bloggera.blog.dto.response.CategoriesResponseDto;
import com.bloggera.blog.model.Category;
import com.bloggera.blog.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoriesResponseDto> getCategories() {

        List<Category> listoFCategories = categoryRepository.findAll();
        List<CategoriesResponseDto> getCategoriesList = listoFCategories.parallelStream().map(list -> {
            CategoriesResponseDto getCategories = new CategoriesResponseDto();
            getCategories.setId(list.getId());
            getCategories.setName(list.getName());
            // getCategoriesList.add(getCategories);
            return getCategories;
        }).toList();

        return getCategoriesList;
    }
}
