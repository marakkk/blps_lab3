package com.blps.lab2.dto;

import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.enums.MonetizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class AppDto {
    private Long id;
    private double appPrice;
    private boolean correctMetaData;
    private boolean correctPermissions;
    private int downloads;
    private boolean inAppPurchases;
    private boolean isChildrenBadPolicy;
    private boolean isNotFree;
    private boolean isViolatesGooglePlayPolicies;
    private MonetizationType monetizationType;
    private String name;
    private double revenue;
    private AppStatus status;
    private double version;
    private Long developerId;
}
