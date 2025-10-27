package com.bloggera.blog.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    private String id;

    private String userId;

    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String subheading;

    private String content;

    private long likeCount;
    private long commentCount;
    private String postImage;

    private List<String> categories;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Post post = (Post) obj;
        return Objects.equals(id, post.id); // Compare by ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Generate hash based on ID
    }
    
}

