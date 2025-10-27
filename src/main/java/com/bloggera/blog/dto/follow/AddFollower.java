package com.bloggera.blog.dto.follow;

import lombok.Data;

@Data
public class AddFollower {
    private String loggedUserId;
    private String followedUserId;
}
