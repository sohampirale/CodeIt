package com.codeit.controller;

import com.codeit.dto.RunRequest;
import com.codeit.dto.RunResponse;
import com.codeit.dto.UserCodeDTO;
import com.codeit.service.Judge0Service;
import com.codeit.service.UserDataService;
import com.codeit.service.JwtService;
import com.codeit.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(
    origins = {
        "https://refactored-winner-pj9x6qjjjgjc7794-8080.app.github.dev",
        "https://code-it-frontend.vercel.app/"
    },
    allowCredentials = "true"
)
public class CodeExecutionController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private Judge0Service judge0Service;

    @Autowired
    private UserDataService userDataService;

    private final String JWT_SECRET = "your-jwt-secret-key";

    @PostMapping("/run")
    public ResponseEntity<?> runCode(
            @RequestBody RunRequest request,
            HttpServletRequest httpRequest
    ) {
        String code = request.getCode();
        code = code.replace("\uFEFF", "");      // common BOM
        // Or more aggressively strip non-ASCII:
        code = code.replaceAll("[^\\x00-\\x7F]", "");
        System.out.println("code received : " + code);        String language = request.getLanguage();

        if (code == null || code.isBlank() || language == null || language.isBlank()) {
            return ResponseEntity.badRequest().body("Insufficient fields");
        }

        String token = null;
        if (httpRequest.getCookies() != null) {
            token = Arrays.stream(httpRequest.getCookies())
                    .filter(c -> "token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        String username = "";
        try {
            if (token != null) {
                Claims claims = Jwts.parser()
                        .setSigningKey(JWT_SECRET.getBytes())
                        .parseClaimsJws(token)
                        .getBody();
                username = claims.get("username", String.class);
            }
        } catch (Exception ignored) {
            // anonymous or invalid token; optional
        }

        try {
            int langId = judge0Service.getLanguageId(language);
            Map<String, Object> resultMap = judge0Service.executeAndWait(langId, code);

            String stdout = resultMap.get("stdout") != null ? resultMap.get("stdout").toString() : "";
            String stderr = resultMap.get("stderr") != null ? resultMap.get("stderr").toString() : "";
            String compileOutput = resultMap.get("compile_output") != null
                    ? resultMap.get("compile_output").toString()
                    : "";

            Map<String, Object> statusMap = (Map<String, Object>) resultMap.get("status");
            String statusDescription = statusMap != null
                    ? statusMap.getOrDefault("description", "Unknown").toString()
                    : "Unknown";

            String outputToStore = !stderr.isBlank() ? stderr
                    : !compileOutput.isBlank() ? compileOutput
                    : stdout;

            userDataService.storeUserCode(username, code, outputToStore, language);

            RunResponse resp = new RunResponse(
                    200,
                    stdout,
                    stderr.isBlank() ? compileOutput : stderr,
                    statusDescription
            );
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error occurred during code execution: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token == null || !jwtService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or missing token");
        }

        String username;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            username = claims.get("username", String.class);
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Invalid token");
        }

        List<UserCodeDTO> history = userDataService.getHistory(username);
        if (history.isEmpty()) {
            return ResponseEntity.status(404).body("No history found");
        }

        return ResponseEntity.ok(history);
    }
}
