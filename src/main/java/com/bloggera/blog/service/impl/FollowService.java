package com.bloggera.blog.service.impl;

import org.springframework.stereotype.Service;

import com.bloggera.blog.model.Follow;
import com.bloggera.blog.repository.FollowRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {
    // private final UserServiceImpl userService;
    private final DelegationServiceImpl userService;
    private final FollowRepository followRepository;

    public Follow addFollower(String loggedUserId,
            String followedUserId) {
        Follow follow = new Follow();
        follow.setFollowingId(followedUserId);
        follow.setFollowerId(loggedUserId);
        return follow;
    }

    public void removeFollower(String loggedUserId,
            String followedUserId) {
        try {
            followRepository.deleteByFollowerIdAndFollowingId(loggedUserId,
                    followedUserId);
        } catch (Exception e) {
            throw new RuntimeException("Error while removing follower: " + e.getMessage());
        }
    }

    public boolean isFollowing(String followerId, String followingId) {
        boolean status = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        return status;
    }

    public void save(Follow follow){
        followRepository.save(follow);
    }

}
