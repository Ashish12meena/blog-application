package com.bloggera.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggera.blog.dto.follow.AddFollower;
import com.bloggera.blog.model.Follow;
import com.bloggera.blog.service.impl.FollowOrchestratorServiceImpl;
import com.bloggera.blog.service.impl.UserServiceImpl;

@CrossOrigin(origins = "*", allowedHeaders = "Authorization")
@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    FollowOrchestratorServiceImpl followService;

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/add")
    public ResponseEntity<?> addFollower(@RequestBody AddFollower addFollower) {
        Follow follow = followService.addFollower(addFollower.getLoggedUserId(), addFollower.getFollowedUserId());
        if (follow != null) {
            return ResponseEntity.ok().body("Follower added");
        } else {
            return ResponseEntity.badRequest().body("Follower not added");
        }

    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFollower(@RequestBody AddFollower addFollower) {

        followService.removeFollower(addFollower.getLoggedUserId(), addFollower.getFollowedUserId());
        return ResponseEntity.ok().body("Follower removed");
    }

    @PostMapping("/status")
    public ResponseEntity<?> status(@RequestBody AddFollower request) {
        boolean status = followService.isFollowing(request.getLoggedUserId(), request.getFollowedUserId());
        return ResponseEntity.ok().body("Follower removed " + status);
    }
}
