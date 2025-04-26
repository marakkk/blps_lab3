package com.blps.lab2.entities.googleplay;

import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.enums.MonetizationType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "app")
public class App {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(nullable = false)
    private double version;

    @Enumerated(EnumType.STRING)
    private AppStatus status;

    private int downloads;
    private double revenue;

    private boolean inAppPurchases;
    private boolean isNotFree;
    private double appPrice;

    @Enumerated(EnumType.STRING)
    private MonetizationType monetizationType;

    private boolean correctPermissions;

    private boolean correctMetadata;

    private boolean isViolatesGooglePlayPolicies;
    private boolean isChildrenBadPolicy;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "developer_id")
    private Developer developer;
}
