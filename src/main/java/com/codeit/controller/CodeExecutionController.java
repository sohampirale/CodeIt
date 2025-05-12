package com.codeit.controller;

import com.codeit.service.UserDataService;
import java.util.stream.Collectors;
import com.codeit.dto.RunRequest;
import com.codeit.dto.RunResponse;
import com.codeit.dto.UserCodeDTO;
import com.codeit.model.CodeRun;
import com.codeit.repository.CodeRunRepository;
import com.codeit.service.Judge0Service;
import com.codeit.service.JwtService;
import com.codeit.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(
    origins = {
      "https://refactored-winner-pj9x6qjjjgjc7794-8080.app.github.dev",
      "https://code-it-frontend.vercel.app"
    },
    allowCredentials = "true"
)
public class CodeExecutionController {

    private final UserDataService userDataService;

    @Autowired
    public CodeExecutionController(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    @Autowired
    private CodeRunRepository codeRunRepo;
    

    @Autowired
    private JwtService jwtService;

    @Autowired
    private Judge0Service judge0Service;

    private final String JWT_SECRET = "WmZ4ak1WcE1ObmV3eGZQaHdZbkxzWjFlYlJ3Y0N5SzI";

    
    @PostMapping("/run")
    public ResponseEntity<?> runCode(
            @RequestBody RunRequest request,
            HttpServletRequest httpRequest
    ) {
        // Clean BOM / non-ASCII
        String code = request.getCode()
                             .replace("\uFEFF", "")
                             .replaceAll("[^\\x00-\\x7F]", "");
        String language = request.getLanguage();

        if (code == null || code.isBlank() || language == null || language.isBlank()) {
            return ResponseEntity.badRequest().body("Insufficient fields");
        }

        // Extract JWT from cookie
        String token = Optional.ofNullable(httpRequest.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> "token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                ).orElse(null);

        String username = "";
        if (token != null && jwtService.validateToken(token)) {
            Claims claims = Jwts.parser()
                                .setSigningKey(JWT_SECRET.getBytes())
                                .parseClaimsJws(token)
                                .getBody();
            username = claims.getSubject();
        }

        try {
            int langId = judge0Service.getLanguageId(language);
            Map<String, Object> resultMap = judge0Service.executeAndWait(langId, code);

            String stdout        = Optional.ofNullable(resultMap.get("stdout")).map(Object::toString).orElse("");
            String stderr        = Optional.ofNullable(resultMap.get("stderr")).map(Object::toString).orElse("");
            String compileOutput = Optional.ofNullable(resultMap.get("compile_output")).map(Object::toString).orElse("");
            @SuppressWarnings("unchecked")
            Map<String, Object> statusMap = (Map<String, Object>) resultMap.get("status");
            String statusDescription = Optional.ofNullable(statusMap)
                                               .map(m -> m.getOrDefault("description", "Unknown").toString())
                                               .orElse("Unknown");

            String outputToStore = !stderr.isBlank() ? stderr
                                 : !compileOutput.isBlank() ? compileOutput
                                 : stdout;

            // ▶️ Persist into MongoDB
            CodeRun entry = new CodeRun(username, code, outputToStore, language);
            codeRunRepo.save(entry);

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
                    .body("Error during code execution: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String,Object>> getHistory(HttpServletRequest request) {
        System.out.println("inside /history");

        // Extract and validate token (cookie or header)
        String token = Optional.ofNullable(request.getCookies())
            .flatMap(cookies -> Arrays.stream(cookies)
                .filter(c -> "token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst())
            .or(() -> {
                String auth = request.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    return Optional.of(auth.substring(7));
                }
                return Optional.empty();
            })
            .orElse(null);

        if (token == null || !jwtService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or missing token");
        }

        Claims claims = Jwts.parser()
                            .setSigningKey(JWT_SECRET.getBytes())
                            .parseClaimsJws(token)
                            .getBody();
        String username = claims.getSubject();
        System.out.println("Fetching history for user: " + username);

        // **Fetch directly from Mongo**
        List<CodeRun> runs = codeRunRepo.findByUsernameOrderByTimestampDesc(username);

        // Map to DTOs
        List<UserCodeDTO> history = runs.stream()
            .map(r -> new UserCodeDTO(
                r.getId(),
                r.getCode(),
                r.getOutput(),
                r.getLanguage()
            ))
            .collect(Collectors.toList());

        // Wrap in {status, history}
        Map<String,Object> body = new HashMap<>();
        body.put("status", 200);
        body.put("history", history);
        return ResponseEntity.ok(body);
    }

}
