package com.codeit.controller;

import com.google.gson.Gson;
import com.codeit.dto.LoginRequest;
import com.codeit.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.ResourceUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import com.codeit.repository.UserRepository;
import com.codeit.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeit.service.JwtService;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {
        "https://refactored-winner-pj9x6qjjjgjc7794-8080.app.github.dev",
        "https://code-it-frontend.vercel.app/"
    },
    allowCredentials = "true"
)
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtService jwtService;

    private static final String USERS_FILE_PATH = "src/main/resources/data/users.json";
    private static final String LOGGED_IN_USERS_FILE_PATH = "src/main/resources/data/loggedInUsers.json";
    private static final String JWT_PW = "your-jwt-secret-key"; // Replace with your JWT secret key

    @GetMapping("/test")
    public String test() {
        return "Test route working!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response    // ← add this parameter
    ) {
        System.out.println("inside /login");
    
        // 1️⃣ Lookup user in MongoDB instead of reading users.json
        Optional<User> optUser = userRepository.findByUsername(request.getUsername());
        if (optUser.isEmpty()) {
            return ResponseEntity
                .status(404)
                .body(new LoginResponse(404, "Username not found", null));
        }
    
        User user = optUser.get();
        if (!user.getPassword().equals(request.getPassword())) {
            // handle failed attempts as before...
            return ResponseEntity
                .status(401)
                .body(new LoginResponse(401, "Incorrect password", null));
        }
    
        // 2️⃣ Generate a real JWT
        String token = jwtService.generateToken(request.getUsername());
    
        // 3️⃣ Set the token cookie on the HttpServletResponse
        ResponseCookie cookie = ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(true)          // true if you’re using HTTPS
            .sameSite("None")      // or "Lax" depending on your needs
            .path("/")
            .maxAge(24 * 60 * 60)  // 1 day
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    
        // 4️⃣ Return the token in the JSON body as well
        return ResponseEntity.ok(
            new LoginResponse(200, "Login successful", token)
        );
    }
    
    // Helper function to read JSON file
    private List<Map<String, Object>> readJsonFile(String filePath) {
        try {
            File file = ResourceUtils.getFile(filePath);
            Scanner scanner = new Scanner(new FileReader(file));
            String json = scanner.useDelimiter("\\A").next();
            return new Gson().fromJson(json, List.class); // Using Gson for JSON parsing
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Helper function to write to JSON file
    private void writeJsonFile(String filePath, List<Map<String, Object>> data) {
        try {
            FileWriter writer = new FileWriter(ResourceUtils.getFile(filePath));
            new Gson().toJson(data, writer); // Using Gson for JSON writing
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        System.out.println("inside / logout");
    ResponseCookie cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0) // Immediately expires
            .build();
        System.out.println("test1");
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    System.out.println("test2");
    return ResponseEntity.ok().body("Logged out successfully.");
}

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> userInput, HttpServletResponse response) {
        System.out.println("inside / signup");
        String username = userInput.get("username");
        String password = userInput.get("password");
        String email = userInput.get("email");

        // Check if username already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "message", "User with username " + username + " exists in the database"
            ));
        }

        // Create and save new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setUnsuccessfulAttempts(0); // Optional

        userRepository.save(user);

        // Generate JWT token (dummy string for now; later use jjwt or auth0)
        String token = "generated-jwt-token"; // <- Replace this

        // Set token as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60 * 24) // 1 day
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of(
            "status", 200,
            "message", "New user created by username: " + username,
            "token", token
        ));
    }


}
