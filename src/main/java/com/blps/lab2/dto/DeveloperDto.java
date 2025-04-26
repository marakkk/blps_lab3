package com.blps.lab2.dto;

import com.blps.lab2.enums.DevAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeveloperDto {
    private Long id;
    private String name;
    private String email;
    private boolean paymentProfile;
    private DevAccount accStatus;
    private double earnings;
    private List<AppDto> apps;
}
