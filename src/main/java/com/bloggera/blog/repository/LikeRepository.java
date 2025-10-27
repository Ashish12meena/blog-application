package com.bloggera.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bloggera.blog.model.Like;



@Repository
public interface LikeRepository extends MongoRepository<Like,String>{
      // Find likes by the postId
    List<Like> findByPostId(String postId);

    // Find likes by the userId
    List<Like> findByUserId(String userId);

    long countByPostId(String postId);

    boolean existsByUserIdAndPostId(String userId, String postId);

    void deleteByUserIdAndPostId(String userId, String postId);

    List<Like> findByUserIdAndPostIdIn(String userId, List<String> postIds);

    Optional<Like> findByUserIdAndPostId(String userId, String postId);
}
