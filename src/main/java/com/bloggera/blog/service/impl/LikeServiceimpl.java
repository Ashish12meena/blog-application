package com.bloggera.blog.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bloggera.blog.model.Like;
import com.bloggera.blog.model.Post;
import com.bloggera.blog.repository.LikeRepository;

@Service
public class LikeServiceimpl {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    @Lazy
    PostServiceImpl postService;

    // Create Like Document

    public Like createLike(Like like) {
        // Create a new Like object
        like.setCreatedAt(LocalDateTime.now());
        // Save and return the like object
        return likeRepository.save(like);
    }

    // get List of Like document for single post

    public List<Like> getLikesForPost(String postId) {
        // Fetch likes based on postId
        return likeRepository.findByPostId(postId);
    }

    // get list of like by user

    public List<Like> getLikesByUser(String userId) {
        // Fetch likes based on userId
        return likeRepository.findByUserId(userId);
    }

    public Long getLikesCountForPost(String postId) {
        return likeRepository.countByPostId(postId);
    }

    public Like getLikeByUserIdAndPostId(String userId, String postId) {
        return likeRepository.findByUserIdAndPostId(userId, postId).orElse(null);
    }

    public boolean checkIfLiked(String postId, String userId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    // get map of Like Status which have combination of postId and boolean value

    public Map<String, Boolean> getLikeStatus(String userId, List<String> postIds) {
        List<Like> likedPosts = likeRepository.findByUserIdAndPostIdIn(userId, postIds);

        Map<String, Boolean> likeStatusMap = likedPosts.stream()
                .collect(Collectors.toMap(
                        Like::getPostId,
                        like -> true, // always mark liked posts as true
                        (existing, replacement) -> existing));

        // ensure posts not in likedPosts get default false
        for (String postId : postIds) {
            likeStatusMap.putIfAbsent(postId, false);
        }

        return likeStatusMap;
    }

    // Add Like document

    @Transactional // Ensures all operations succeed together
    public Like addLike(String postId, String userId) {
        // Check if the post exists before proceeding
        Post post = postService.getPostsByPostId(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found with ID: " + postId);
        }

        // Create and save the like
        Like like = new Like();
        like.setCreatedAt(LocalDateTime.now());
        like.setPostId(postId);
        like.setUserId(userId);
        Like savedLike = likeRepository.save(like); // Save first

        // Increment like count in the post
        postService.incrementLikeCount(postId);

        return savedLike;
    }

    public void removeLike(String postId, String userId) {
        // Check if the like exists before attempting to remove it
        Like like = getLikeByUserIdAndPostId(userId, postId);

        if (like != null) {
            // Remove like and decrement count
            likeRepository.deleteByUserIdAndPostId(userId, postId);
            postService.decrementLikeCount(postId);
            // Delete associated notification
        } else {
        }
    }

    // get count of like and comment of post

    public long getCountLike(String postId) {
        return getLikesCountForPost(postId);
    }
}
