package com.bloggera.blog.dto.response;


import lombok.Data;

@Data
public class UserDetailsResponse {
    private String userId;
    private String email;
    private String username;
    private String profilePicture;
}
