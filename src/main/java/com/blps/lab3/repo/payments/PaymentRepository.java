package com.blps.lab3.repo.payments;

import com.blps.lab3.entities.payments.Payment;
import com.blps.lab3.enums.PaymentStatus;
import com.blps.lab3.entities.payments.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAppId(Long appId);

    boolean existsByAppIdAndUserIdAndStatus(Long appId, Long userId, PaymentStatus status);
}
