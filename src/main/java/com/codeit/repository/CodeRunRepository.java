package com.codeit.repository;

import com.codeit.model.CodeRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CodeRunRepository extends MongoRepository<CodeRun, String> {
    List<CodeRun> findByUsernameOrderByTimestampDesc(String username);
}
