package com.blps.lab2.repo.payments;

import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAppId(Long appId);

    boolean existsByAppIdAndUserIdAndStatus(Long appId, Long userId, PaymentStatus paymentStatus);
}
