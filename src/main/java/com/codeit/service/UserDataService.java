package com.codeit.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.codeit.dto.UserCodeDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserDataService {

    private final Map<String, List<UserCodeDTO>> userHistory = new HashMap<>();

    private static final String USER_DATA_FILE = "src/main/resources/data/userData.json";
    private final Gson gson = new Gson();

    public void storeUserCode(String username, String code, String output, String language) {
        try {
            // Read existing users
            List<Map<String, Object>> users = readUsers();

            // Find or create user
            Map<String, Object> user = users.stream()
                    .filter(u -> username.equals(u.get("username")))
                    .findFirst()
                    .orElseGet(() -> {
                        Map<String, Object> newUser = new LinkedHashMap<>();
                        newUser.put("username", username);
                        newUser.put("totalCodes", 0.0); // gson reads numbers as doubles
                        newUser.put("history", new ArrayList<Map<String, Object>>());
                        users.add(newUser);
                        return newUser;
                    });

            // Update code count and history
            int newId = ((Double) user.get("totalCodes")).intValue() + 1;
            user.put("totalCodes", (double) newId);

            List<Map<String, Object>> history = (List<Map<String, Object>>) user.get("history");
            Map<String, Object> codeEntry = new LinkedHashMap<>();
            codeEntry.put("id", newId);
            codeEntry.put("code", code);
            codeEntry.put("output", output);
            codeEntry.put("language", language);
            history.add(codeEntry);

            // Save back to file
            writeUsers(users);

            System.out.println("New code added by " + username + " in userData.json");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, Object>> readUsers() throws IOException {
        FileReader reader = new FileReader(USER_DATA_FILE);
        Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
        return gson.fromJson(reader, type);
    }

    public List<UserCodeDTO> getHistory(String username) {
        try {
            List<Map<String, Object>> users = readUsers();
            Optional<Map<String, Object>> opt =
                users.stream().filter(u -> username.equals(u.get("username"))).findFirst();
            if (opt.isEmpty()) return Collections.emptyList();

            List<Map<String, Object>> rawHistory =
                (List<Map<String, Object>>) opt.get().get("history");
            List<UserCodeDTO> dtos = new ArrayList<>();
            for (Map<String, Object> e : rawHistory) {
                int id = ((Double) e.get("id")).intValue();
                String code = (String) e.get("code");
                String output = (String) e.get("output");
                String lang = (String) e.get("language");
                dtos.add(new UserCodeDTO(id, code, output, lang));
            }
            return dtos;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void writeUsers(List<Map<String, Object>> users) throws IOException {
        FileWriter writer = new FileWriter(USER_DATA_FILE);
        gson.toJson(users, writer);
        writer.flush();
        writer.close();
    }
}
