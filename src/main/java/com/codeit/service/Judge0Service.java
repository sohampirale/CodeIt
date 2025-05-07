package com.codeit.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    private static final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions";
    private static final String RAPID_API_KEY = "a76d47cbbamsheb3ef468eef8a5dp1a6a13jsnddcdfc9837b0";      
    private static final String RAPID_API_HOST = "judge0-ce.p.rapidapi.com";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    /** 
     * Submits code and blocks until Judge0 returns the full result.
     */
    public Map<String, Object> executeAndWait(int languageId, String sourceCode) {
        String url = JUDGE0_URL + "?base64_encoded=false&wait=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", RAPID_API_KEY);
        headers.set("X-RapidAPI-Host", RAPID_API_HOST);

        String body = String.format(
            "{\"source_code\":\"%s\",\"language_id\":%d,\"stdin\":\"\"}",
            escapeJson(sourceCode), languageId
        );

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(response.getBody(), type);
    }

    /** Map your language names to Judge0 IDs */
    public int getLanguageId(String language) {
        Map<String, Integer> map = new HashMap<>();
        map.put("cpp", 54);
        map.put("c", 50);
        map.put("java", 62);
        map.put("python", 71);
        map.put("javascript", 63);
        // add more as needed
        Integer id = map.get(language.toLowerCase());
        if (id == null) throw new IllegalArgumentException("Unsupported language: " + language);
        return id;
    }

    /** Escape quotes for JSON body */
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
