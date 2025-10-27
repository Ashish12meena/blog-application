package com.bloggera.blog.dto.user;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserCardDetails {

    private String postId;
    private String postTitle;
    private String postContent;
    private String postImage;
    private long likeCount;
    private boolean likeStatus;
}
