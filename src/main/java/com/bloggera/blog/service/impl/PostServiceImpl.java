package com.bloggera.blog.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bloggera.blog.dto.post.ExcludedIds;
import com.bloggera.blog.dto.response.GetAllPostCardDetails;
import com.bloggera.blog.dto.response.GetFullPostDetail;
import com.bloggera.blog.exception.UserNotFoundException;
import com.bloggera.blog.model.Post;
import com.bloggera.blog.model.User;
import com.bloggera.blog.repository.PostRepository;


// import com.example.Blogera_demo.repository.LikeRepository;

@Service
public class PostServiceImpl {
    private final UserServiceImpl userService;

    public PostServiceImpl(@Lazy UserServiceImpl userService) {
        this.userService = userService;
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeServiceimpl likeService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ImageUploaderService imageUploaderService;

    @Autowired
    Executor taskExecutor;

    // Create Post

    public Post createPost(String userId, Post post) {

        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isPresent()) {
            post.setUserId(userOptional.get().getId()); // Set the user as the author of the post
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post = postRepository.save(post);
            userService.incrementPostCount(userId);
            return post;
        } else {
            // Handle user not found scenario
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    // get Post By UserId
    public List<Post> getPostsByUserId(String email) {
        return postRepository.findByUserId(email);
    }

    // get Post By PostId
    public Post getPostsByPostId(String postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.get();
    }

    // get GetFullPostDetail By postId and UserId
    public GetFullPostDetail getFullPostDetails(String postId, String currentUserId) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        CompletableFuture<Post> postFuture = CompletableFuture.supplyAsync(() -> getPostsByPostId(postId), executor);

        CompletableFuture<Boolean> likeStatusFuture = CompletableFuture
                .supplyAsync(() -> likeService.checkIfLiked(postId, currentUserId), executor);

        Post post = postFuture.join();
        CompletableFuture<User> userFuture = CompletableFuture
                .supplyAsync(() -> userService.findById(post.getUserId()).orElse(null), executor);

        User user = userFuture.join();

        boolean status = likeStatusFuture.join();

        executor.shutdown();

        String username = Optional.ofNullable(user).map(User::getUsername).orElse("Unknown");
        String profilePicture = Optional.ofNullable(user).map(User::getProfilePicture).orElse("");

        GetFullPostDetail getFullPostDetail = new GetFullPostDetail();
        getFullPostDetail.setCommentCount(post.getCommentCount());
        getFullPostDetail.setLikeCount(post.getLikeCount());
        getFullPostDetail.setPostContent(post.getContent());
        getFullPostDetail.setPostImage(post.getPostImage());
        getFullPostDetail.setPostTitle(post.getTitle());
        getFullPostDetail.setProfilePicture(profilePicture);
        getFullPostDetail.setUsername(username);
        getFullPostDetail.setLikeStatus(status);
        getFullPostDetail.setUserEmail(user.getEmail());

        return getFullPostDetail;
    }

    // get List of post Card Details of specific user

    public List<GetAllPostCardDetails> getCardDetails(String currentUserId, Set<String> excludedIds,
            List<String> categories) {

        // Fetch posts in a single query
        List<Post> posts = new ArrayList<>();
        try {
            posts = getAllFilteredPosts(excludedIds, categories);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        }
        if (posts.isEmpty())
            return Collections.emptyList(); // Return early if no posts

        // Extract user IDs & post IDs
        List<String> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<String> postIds = posts.stream().map(Post::getId).distinct().toList();

        // Fetch users and like status concurrently
        ExecutorService executor = Executors.newFixedThreadPool(3); // Use limited threads
        CompletableFuture<Map<String, User>> userFuture = CompletableFuture.supplyAsync(
                () -> userService.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, user -> user)),
                executor);
        CompletableFuture<Map<String, Boolean>> likeFuture = CompletableFuture.supplyAsync(
                () -> likeService.getLikeStatus(currentUserId, postIds),
                executor);

        // Wait for both tasks to complete
        Map<String, User> userMap = userFuture.join();
        Map<String, Boolean> likeStatusMap = likeFuture.join();

        // Process posts in parallel
        List<GetAllPostCardDetails> postDetailsList = posts.parallelStream()
                .map(post -> {
                    User user = userMap.get(post.getUserId());
                    GetAllPostCardDetails details = new GetAllPostCardDetails();
                    details.setPostId(post.getId());
                    details.setPostTitle(post.getTitle());
                    details.setPostContent(post.getContent());
                    details.setPostImage(post.getPostImage());
                    details.setLikeCount(post.getLikeCount());
                    details.setCommentCount(post.getCommentCount());
                    details.setLikeStatus(likeStatusMap.getOrDefault(post.getId(), false));

                    if (user != null) {
                        details.setUsername(user.getUsername());
                        details.setProfilePicture(user.getProfilePicture());
                        details.setUserEmail(user.getEmail());
                    }
                    return details;
                }).toList(); // Parallel processing for better performance

        executor.shutdown(); // Shutdown executor

        return postDetailsList;
    }

    // get list of All PostCardDetails
    public List<GetAllPostCardDetails> getData() {
        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch all users in a single query to avoid N+1 problem
        List<String> userIds = posts.stream().map(Post::getUserId).toList();
        Map<String, User> userMap = userService.findAllById(userIds)
                .stream().collect(Collectors.toMap(User::getId, user -> user));

        // Process each post asynchronously using Spring's thread pool
        List<CompletableFuture<GetAllPostCardDetails>> futures = posts.stream()
                .map(post -> CompletableFuture.supplyAsync(() -> mapPostToDetails(post, userMap), taskExecutor))
                .toList();

        // Wait for all threads to complete and collect results
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private GetAllPostCardDetails mapPostToDetails(Post post, Map<String, User> userMap) {
        User user = userMap.get(post.getUserId());
        GetAllPostCardDetails details = new GetAllPostCardDetails();
        details.setCommentCount(post.getCommentCount());
        details.setLikeCount(post.getLikeCount());
        details.setPostContent(post.getContent());
        details.setPostId(post.getId());
        details.setPostImage(post.getPostImage());
        details.setPostTitle(post.getTitle());

        if (user != null) {
            details.setUsername(user.getUsername());
            details.setProfilePicture(user.getProfilePicture());
        }

        return details;
    }

    public void incrementLikeCount(String postId) {

        Query query = new Query(Criteria.where("id").is(postId));
        Update update = new Update().inc("likeCount", 1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    public void decrementLikeCount(String postId) {
        Query query = new Query(Criteria.where("id").is(postId));
        Update update = new Update().inc("likeCount", -1); // Decrease likeCount by 1
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    public void incrementCommentCount(String postId) {
        Query query = new Query(Criteria.where("id").is(postId));
        Update update = new Update().inc("commentCount", 1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    public void decrementCommentCount(String postId) {
        Query query = new Query(Criteria.where("id").is(postId));
        Update update = new Update().inc("commentCount", -1); // Increment likeCount by 1
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    public ResponseEntity<String> addPost(String userId, Post post, MultipartFile postImage) {
        if (postImage == null || postImage.isEmpty()) {
            createPost(userId, post);
            return ResponseEntity.ok("Post added successfully, no image uploaded.");
        }

        String url = imageUploaderService.uploadImage(postImage);
        if (!url.isEmpty()) {
            post.setPostImage(url);
            createPost(userId, post);
            return ResponseEntity.ok(url);
        }

        return ResponseEntity.badRequest().body("Image upload failed.");
    }

    public List<Post> getAllPosts(Set<String> excludedIds) {
        AggregationOperation matchStage = Aggregation.match(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").nin(excludedIds));

        AggregationOperation sampleStage = Aggregation.sample(30);
        Aggregation aggregation = Aggregation.newAggregation(matchStage, sampleStage);
        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);

        return results.getMappedResults();
    }

    public List<Post> getAllFilteredPosts(Set<String> excludedIds, List<String> categories)
            throws InterruptedException, ExecutionException {

        Set<Post> uniquePosts = ConcurrentHashMap.newKeySet();

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try {
            // Submit tasks to run in parallel
            List<Future<List<Post>>> futures = new ArrayList<>();
            futures.add(executorService.submit(() -> getMostLikedPosts(excludedIds)));
            futures.add(executorService.submit(() -> getMostCommentedPosts(excludedIds)));
            futures.add(executorService.submit(() -> getMostSimilartoCategoryPosts(excludedIds, categories)));
            futures.add(executorService.submit(() -> getRandomPosts(excludedIds)));

            // Collect the results
            for (Future<List<Post>> future : futures) {
                uniquePosts.addAll(future.get()); // Add list elements to the set
            }
        } finally {
            executorService.shutdown();
        }

        List<Post> finalPosts = new ArrayList<>(uniquePosts);

        return finalPosts;

    }

    public List<Post> getRandomPosts(Set<String> excludedIds) {
        if (excludedIds == null) {
            excludedIds = new HashSet<>();
        }

        // Match stage to exclude specific IDs
        AggregationOperation matchStage = Aggregation.match(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").nin(excludedIds));

        // Sample stage to get 10 random posts
        AggregationOperation sampleStage = Aggregation.sample(10);

        // Aggregation pipeline with match and sample
        Aggregation aggregation = Aggregation.newAggregation(matchStage, sampleStage);

        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);

        return results.getMappedResults();
    }

    public List<Post> getMostLikedPosts(Set<String> excludedIds) {
        Criteria criteria = new Criteria();
        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteria.and("_id").nin(excludedIds);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "likeCount")),
                Aggregation.limit(10));

        return mongoTemplate.aggregate(aggregation, "post", Post.class).getMappedResults();
    }

    public List<Post> getMostCommentedPosts(Set<String> excludedIds) {
        Criteria criteria = new Criteria();
        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteria.and("_id").nin(excludedIds);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "commentCount")),
                Aggregation.limit(10));

        return mongoTemplate.aggregate(aggregation, "post", Post.class).getMappedResults();
    }

    public List<Post> getMostSimilartoCategoryPosts(Set<String> excludedIds, List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        Criteria criteria = Criteria.where("categories").in(categories);
        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteria.and("_id").nin(excludedIds);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sample(10));

        return mongoTemplate.aggregate(aggregation, "post", Post.class).getMappedResults();
    }

    // Search based on list of categories
    private List<Post> searchByCategories(Set<String> excludedIds, List<String> categories, Integer sample) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteriaList.add(Criteria.where("_id").nin(excludedIds));
        }

        criteriaList.add(Criteria.where("categories").in(categories));

        return executeAggregation(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])), sample);
    }

    // search based on categories than filtered out from them on the base of text
    public List<Post> searchByCategoriesThenText(Set<String> excludedIds, List<String> categories, String searchText,
            Integer sample) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        Criteria criteria = Criteria.where("categories").in(categories);

        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteria.and("_id").nin(excludedIds);
        }

        // Fetch posts based on categories & excluded IDs only
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sample(sample != null ? sample : 10) // Fetch more posts before filtering by title
        );

        List<Post> posts = mongoTemplate.aggregate(aggregation, "post", Post.class).getMappedResults();

        // Now filter based on title text in Java
        if (searchText != null && !searchText.isBlank()) {
            posts = posts.stream()
                    .filter(post -> post.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return posts;
    }

    private List<Post> executeAggregation(Criteria criteria, Integer sample) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sample(sample != null ? sample : 10) // Limit results
        );

        return mongoTemplate.aggregate(aggregation, "post", Post.class).getMappedResults();
    }

    // search based on text only
    public List<Post> searchByText(Set<String> excludedIds, String searchText, Integer sample) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (excludedIds != null && !excludedIds.isEmpty()) {
            criteriaList.add(Criteria.where("_id").nin(excludedIds));
        }

        if (searchText != null && !searchText.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(searchText, "i")); // Case-insensitive text search
        }

        return executeAggregation(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])), sample);
    }

    // Search based on text
    public List<GetAllPostCardDetails> searchPostByText(ExcludedIds excludedIds) {
        List<Post> posts = new ArrayList<>();
        if (excludedIds.getListOfCategories() == null || excludedIds.getListOfCategories().isEmpty()) {
            posts = searchByText(excludedIds.getExcludedIds(), excludedIds.getText(), excludedIds.getSample());
        } else {
            posts = searchByCategories(excludedIds.getExcludedIds(), excludedIds.getListOfCategories(),
                    excludedIds.getSample());
        }

        List<GetAllPostCardDetails> getAllPostCardDetails = new ArrayList<>();
        getAllPostCardDetails = posts.stream().map(post -> {
            GetAllPostCardDetails getCardDetail = new GetAllPostCardDetails();
            getCardDetail.setCommentCount(post.getCommentCount());
            getCardDetail.setLikeCount(post.getLikeCount());
            getCardDetail.setPostContent(post.getContent());
            getCardDetail.setPostId(post.getId());
            getCardDetail.setPostImage(post.getPostImage());
            getCardDetail.setPostTitle(post.getTitle());
            return getCardDetail;
        }).collect(Collectors.toList());

        // System.out.println(posts);
        return getAllPostCardDetails;
    }

    public List<Post> getPostsByPostIds(List<String> postIds) {
        return postRepository.findAllById(postIds);
    }

}