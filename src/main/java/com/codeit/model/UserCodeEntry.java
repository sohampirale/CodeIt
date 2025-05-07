package com.codeit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_code_history")
public class UserCodeEntry {

    @Id
    private String id;
    private String username;
    private String code;
    private String output;
    private String language;
    private Instant timestamp;

    // No-arg constructor required by Spring Data
    public UserCodeEntry() {}

    // Convenience constructor
    public UserCodeEntry(String username, String code, String output, String language) {
        this.username  = username;
        this.code      = code;
        this.output    = output;
        this.language  = language;
        this.timestamp = Instant.now();
    }

    // ----- Getters & Setters -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
