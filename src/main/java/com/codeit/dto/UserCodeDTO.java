package com.codeit.dto;

public class UserCodeDTO {
    private String id;
    private String code;
    private String output;
    private String language;

    public UserCodeDTO() {}

    public UserCodeDTO(String id, String code, String output, String language) {
        this.id       = id;
        this.code     = code;
        this.output   = output;
        this.language = language;
    }

    // ───── Getters & Setters ───────────────────────────────────────────────────

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
}
