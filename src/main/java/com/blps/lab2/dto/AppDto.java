package com.blps.lab2.dto;

import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.enums.MonetizationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppDto {
    private Long id;
    private String name;
    private double version;
    private AppStatus status;
    private int downloads;
    private double revenue;
    private boolean inAppPurchases;
    private boolean isNotFree;
    private double appPrice;
    private MonetizationType monetizationType;
}
