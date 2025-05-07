package com.codeit.repository;

import com.codeit.model.UserCodeEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserCodeEntryRepository extends MongoRepository<UserCodeEntry, String> {
    List<UserCodeEntry> findByUsername(String username);
}
