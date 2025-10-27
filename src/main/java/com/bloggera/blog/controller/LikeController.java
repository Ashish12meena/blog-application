package com.bloggera.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.like.LikeDetails;
import com.bloggera.blog.model.Like;
import com.bloggera.blog.service.impl.LikeServiceimpl;

@RestController
@RequestMapping("api/like")
public class LikeController {
    @Autowired
    LikeServiceimpl likeService;

    @PostMapping
    public ResponseEntity<Like> createLike(@RequestBody Like like) {
        Like liked = likeService.createLike(like);
        return ResponseEntity.status(HttpStatus.CREATED).body(liked);
    }

    @PostMapping("/likeStatus")
    public boolean checkIfLiked(@RequestBody LikeDetails likeDetails) {

        return likeService.checkIfLiked(likeDetails.getPostId(), likeDetails.getUserId());
    }

    @PostMapping("/addlike")
    public boolean addLike(@RequestBody LikeDetails likeDetails) {

        Like like = likeService.addLike(likeDetails.getPostId(), likeDetails.getUserId());
        if (like != null) {
            return true;
        }
        return false;
    }

    @PostMapping("/removelike")
    public boolean removeLike(@RequestBody LikeDetails likeDetails) {
        likeService.removeLike(likeDetails.getPostId(), likeDetails.getUserId());
        return true;
    }

    @GetMapping("/{postId}/count")
    public long countLike(@PathVariable String postId) {
        return likeService.getCountLike(postId);
    }

}
