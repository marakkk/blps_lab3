package com.blps.lab2.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage implements Serializable {
    private Long userId;
    private Long appId;
}