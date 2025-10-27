package com.bloggera.blog.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "follow")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Follow {
  @Id
  private String id;
  private String followingId;  //The user who is being followed.
  private String followerId;  //The user who is following someone.

    
}