package com.codeit.dto;

public class RunResponse {
    private int statusCode;
    private String stdout;
    private String stderr;
    private String status;

    public RunResponse(int statusCode, String stdout, String stderr, String status) {
        this.statusCode = statusCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.status = status;
    }
    
    // Getters
    public int getStatusCode() {
        return statusCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
