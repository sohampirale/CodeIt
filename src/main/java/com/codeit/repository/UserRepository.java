// src/main/java/com/codeit/repository/UserRepository.java
package com.codeit.repository;

import com.codeit.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
