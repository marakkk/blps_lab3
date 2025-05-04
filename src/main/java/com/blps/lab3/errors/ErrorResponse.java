package com.blps.lab3.errors;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String details;

    public ErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
    }

}

