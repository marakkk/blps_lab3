package com.blps.lab3.entities.payments;

import com.blps.lab3.entities.googleplay.AppUser;
import com.blps.lab3.entities.googleplay.Developer;
import com.blps.lab3.enums.MonetizationType;
import com.blps.lab3.enums.PaymentStatus;
import com.blps.lab3.enums.MonetizationType;
import com.blps.lab3.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
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

    private Long userId;
}
