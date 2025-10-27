package com.bloggera.blog.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bloggera.blog.model.Category;



@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
}
