package com.bloggera.blog.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bloggera.blog.model.Follow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowOrchestratorServiceImpl {
    private final FollowService followService;
    private final UserServiceImpl userService;


    @Transactional
    public Follow addFollower(String loggedUserId,
            String followedUserId) {
       Follow follow = addFollower(loggedUserId, followedUserId);
        try {
            userService.incrementFollowerCount(followedUserId);

            userService.incrementFollowingCount(loggedUserId);
        } catch (Exception e) {
            throw new RuntimeException("Error while adding follower: " + e.getMessage());
        }
        return follow;
    }


    @Transactional
    public void removeFollower(String loggedUserId,
            String followedUserId) {
        try {
            followService.removeFollower(loggedUserId, followedUserId);
            userService.decrementFollowerCount(followedUserId);
            userService.decrementFollowingCount(loggedUserId);
        } catch (Exception e) {
            throw new RuntimeException("Error while removing follower: " + e.getMessage());
        }
    }


    public boolean isFollowing(String loggedUserId, String followedUserId) {
        return followService.isFollowing(followedUserId, followedUserId);
    }

}
