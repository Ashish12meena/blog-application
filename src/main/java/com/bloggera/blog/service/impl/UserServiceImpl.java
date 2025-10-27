package com.bloggera.blog.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bloggera.blog.dto.user.GetUserCardDetails;
import com.bloggera.blog.model.Post;
import com.bloggera.blog.model.User;
import com.bloggera.blog.repository.UserRepository;

@Service
public class UserServiceImpl {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private LikeServiceimpl likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private MongoTemplate mongoTemplate;

    // Retrieve a user by ID
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    // Retrieve all users

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<String> getAllUserId() {
        return userRepository.findAll()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    // Update a user

    public User updateUser(String id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new NoSuchElementException("User with ID " + id + " not found");
        }
        User user = optionalUser.get();
        // Ensure user details are valid
        if (userDetails.getUsername() != null)
            user.setUsername(userDetails.getUsername());
        if (userDetails.getEmail() != null)
            user.setEmail(userDetails.getEmail());
        if (userDetails.getProfilePicture() != null)
            user.setProfilePicture(userDetails.getProfilePicture());
        return userRepository.save(user);
    }

    // Delete a user

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    public ResponseEntity<?> getUserCardData(String loginedUserId,
            String anotherUserId) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Optional<User> optionalUser = userRepository.findById(loginedUserId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();
        List<Post> posts = postService.getPostsByUserId(user.getId());
        boolean followStatus = followService.isFollowing(anotherUserId, user.getId());

        // Always return user details, even if posts are empty
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "userId", user.getId(),
                "followStatus", followStatus,
                "followerCount", user.getFollowerCount(),
                "followingCount", user.getFollowingCount(),
                "postCount", user.getPostCount(),
                "userBio", Optional.ofNullable(user.getBio()).orElse(""),
                "dateOfBirth", Optional.ofNullable(user.getDateOfBirth()),
                "profilePicture", Optional.ofNullable(user.getProfilePicture()).orElse("")));
        responseBody.put("posts", new ArrayList<>()); // Default to an empty array

        if (posts == null || posts.isEmpty()) {
            return ResponseEntity.ok(responseBody); // ✅ Return user with empty posts array
        }

        List<String> postIds = posts.stream().map(Post::getId).distinct().toList();

        // Fetch likes asynchronously
        CompletableFuture<Map<String, Boolean>> likeFuture = CompletableFuture.supplyAsync(
                () -> likeService.getLikeStatus(user.getId(), postIds), executor);

        // Fetch post details asynchronously
        List<CompletableFuture<GetUserCardDetails>> postFutures = posts.stream()
                .map(post -> CompletableFuture.supplyAsync(() -> {

                    boolean likeStatus = likeFuture.join().getOrDefault(post.getId(), false);

                    GetUserCardDetails getCardDetails = new GetUserCardDetails();
                    getCardDetails.setPostId(post.getId());
                    getCardDetails.setLikeCount(post.getLikeCount());
                    getCardDetails.setPostContent(post.getContent());
                    getCardDetails.setPostImage(null);
                    getCardDetails.setPostTitle(post.getTitle());
                    getCardDetails.setLikeStatus(likeStatus);
                    
                    return getCardDetails;
                }, executor))
                .collect(Collectors.toList());

        // Wait for all futures to complete
        List<GetUserCardDetails> getCardDetailsList = postFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        responseBody.put("posts", getCardDetailsList); // ✅ Only replace empty posts if there are actual posts

        return ResponseEntity.ok().body(responseBody);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }

    public void incrementFollowerCount(String userId) {

        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("followerCount", 1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void decrementFollowerCount(String userId) {

        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("followerCount", -1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void incrementFollowingCount(String userId) {

        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("followingCount", 1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void decrementFollowingCount(String userId) {

        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("followingCount", -1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void incrementPostCount(String userId) {
        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("postCount", 1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void decrementPostCount(String userId) {

        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update().inc("postCount", -1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public List<User> findAllById(List<String> userIds) {
        return userRepository.findAllById(userIds);

    }
    

}
