// src/main/java/com/codeit/model/User.java
package com.codeit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;

    // Add other fields like unsuccessfulAttempts if needed
    private int unsuccessfulAttempts;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getUnsuccessfulAttempts() { return unsuccessfulAttempts; }
    public void setUnsuccessfulAttempts(int attempts) { this.unsuccessfulAttempts = attempts; }
}
