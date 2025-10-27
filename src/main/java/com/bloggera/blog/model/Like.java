package com.bloggera.blog.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "likes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Like {

    @Id
    private String id; // Unique identifier for each like

    private String userId; // The user who liked the post or comment
    private String postId; // The post or comment that was liked (You could also use a generic "contentId" for posts/comments)
    
    private LocalDateTime createdAt; // Timestamp when the like was created
}
