package com.bloggera.blog.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bloggera.blog.dto.like.LikeDetails;
import com.bloggera.blog.dto.post.ExcludedIds;
import com.bloggera.blog.dto.response.GetAllPostCardDetails;
import com.bloggera.blog.dto.response.GetFullPostDetail;
import com.bloggera.blog.model.Post;
import com.bloggera.blog.service.impl.PostServiceImpl;

@CrossOrigin(origins = "*", allowedHeaders = "Authorization")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostServiceImpl postService;

    @PostMapping("/user/{userId}")
    public Post createPost(@PathVariable String userId, @RequestBody Post post) {
        logger.info("Creating post for userId: {}", userId);
        return postService.createPost(userId, post);
    }

    @PostMapping("/cardDetails")
    public List<GetAllPostCardDetails> getCartDetails(@RequestBody ExcludedIds excludedIds) {
        logger.info("Fetching card details for userId: {}, excludedIds count: {}", 
            excludedIds.getUserId(), excludedIds.getExcludedIds() != null ? excludedIds.getExcludedIds().size() : 0);
        List<GetAllPostCardDetails> getAllPostCardDetails = postService.getCardDetails(excludedIds.getUserId(),
                excludedIds.getExcludedIds(), excludedIds.getListOfCategories());
        logger.info("Retrieved {} card details", getAllPostCardDetails.size());
        return getAllPostCardDetails;
    }

    @PostMapping("/data")
    public List<GetAllPostCardDetails> getData() {
        logger.info("Fetching all data");
        List<GetAllPostCardDetails> getAllPostCardDetails = postService.getData();
        return getAllPostCardDetails;
    }

    @PostMapping("/fullPostDetails")
    public GetFullPostDetail getFullPostDetails(@RequestBody LikeDetails likedetails) {
        logger.info("Fetching full post details for postId: {}, userId: {}", 
            likedetails.getPostId(), likedetails.getUserId());
        return postService.getFullPostDetails(likedetails.getPostId(), likedetails.getUserId());
    }

    @PostMapping("/addPost")
    public ResponseEntity<?> addPost(
            @RequestParam("userId") String userId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") List<String> category,
            @RequestPart(value = "postImage", required = false) MultipartFile postImage) {

        logger.info("=== ADD POST REQUEST ===");
        logger.info("userId: {}", userId);
        logger.info("title: {}", title);
        logger.info("content length: {}", content != null ? content.length() : 0);
        logger.info("categories: {}", category);
        logger.info("postImage: {}", postImage != null ? postImage.getOriginalFilename() : "null");

        if (userId == null || userId.trim().isEmpty()) {
            logger.error("UserId is null or empty!");
            return ResponseEntity.badRequest().body("UserId is required");
        }

        if (title == null || title.trim().isEmpty()) {
            logger.error("Title is null or empty!");
            return ResponseEntity.badRequest().body("Title is required");
        }

        if (content == null || content.trim().isEmpty()) {
            logger.error("Content is null or empty!");
            return ResponseEntity.badRequest().body("Content is required");
        }

        if (category == null || category.isEmpty()) {
            logger.error("Categories list is null or empty!");
            return ResponseEntity.badRequest().body("At least one category is required");
        }

        Post post = new Post();
        post.setContent(content);
        post.setTitle(title);
        post.setCategories(category);
        
        ResponseEntity<?> response = postService.addPost(userId, post, postImage);
        logger.info("Post creation response: {}", response.getStatusCode());
        return response;
    }

    @PostMapping("/getAllPost")
    public List<Post> getAllPosts(@RequestBody ExcludedIds excludedIds) {
        logger.info("Getting all posts with {} excluded IDs", 
            excludedIds.getExcludedIds() != null ? excludedIds.getExcludedIds().size() : 0);
        return postService.getAllPosts(excludedIds.getExcludedIds());
    }

    @PostMapping("/getMostLiked")
    public List<Post> getMostLikedPosts(@RequestBody ExcludedIds excludedIds) {
        return postService.getMostLikedPosts(excludedIds.getExcludedIds());
    }

    @PostMapping("/getMostComment")
    public List<Post> getMostCommentPosts(@RequestBody ExcludedIds excludedIds) {
        return postService.getMostCommentedPosts(excludedIds.getExcludedIds());
    }

    @PostMapping("/getAllPosts")
    public List<Post> getAllFilteredPostss(@RequestBody ExcludedIds excludedIds) {
        List<Post> filteredPost = new ArrayList<>();

        try {
            filteredPost = postService.getAllFilteredPosts(excludedIds.getExcludedIds(),
                    excludedIds.getListOfCategories());
        } catch (InterruptedException e) {
            logger.error("Interrupted exception while getting filtered posts", e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            logger.error("Execution exception while getting filtered posts", e);
            e.printStackTrace();
        }
        return filteredPost;
    }

    @PostMapping("/getRandom")
    public List<Post> getRandomPost(@RequestBody ExcludedIds excludedIds) {
        return postService.getRandomPosts(excludedIds.getExcludedIds());
    }

    @PostMapping("/search")
    public List<GetAllPostCardDetails> searchPosts(@RequestBody ExcludedIds excludedIds) {
        logger.info("Searching posts with text: {}, categories: {}", 
            excludedIds.getText(), excludedIds.getListOfCategories());
        return postService.searchPostByText(excludedIds);
    }
}