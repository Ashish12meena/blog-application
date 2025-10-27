package com.bloggera.blog.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

import com.bloggera.blog.model.Follow;



@Repository
public interface FollowRepository extends  MongoRepository<Follow,String> {
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
}
