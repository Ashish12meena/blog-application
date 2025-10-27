
package com.bloggera.blog.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.bloggera.blog.model.User;


@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findById(String id);

    User save(Optional<User> user);

    User findByUsername(String username);

    @Query("{ 'email' : ?0 }")
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    
}
