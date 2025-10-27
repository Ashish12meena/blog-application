package com.bloggera.blog.repository;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bloggera.blog.model.PostImage;


public interface PostImageReposiroty extends MongoRepository<PostImage, String> {
    List<PostImage> findByPostId(String postId);
}
    