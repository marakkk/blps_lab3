package com.blps.lab2.entities.payments;

import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.googleplay.Developer;
import com.blps.lab2.enums.MonetizationType;
import com.blps.lab2.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double amount;

    @Enumerated(EnumType.STRING)
    private MonetizationType monetizationType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Long developerId;
    private Long appId;
}
