package com.bloggera.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllPostCardDetails {
    private String postId;
    private String username;
    private String profilePicture;
    private String postContent;
    private String postTitle;
    private long likeCount;
    private long commentCount;
    private String postImage;
    private String userEmail;
    private boolean likeStatus;
}
