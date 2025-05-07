package com.blps.lab3.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage implements Serializable {
    private Long userId;
    private Long appId;
    private String error;
}