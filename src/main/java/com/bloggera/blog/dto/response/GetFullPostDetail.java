package com.bloggera.blog.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetFullPostDetail {
    private String username;
    private String profilePicture;
    private String postTitle;
    private String postContent;
    private String postImage; 
    private long likeCount;
    private List<String> comments;
    private long commentCount;
    private String userEmail;
    private boolean likeStatus;
}
