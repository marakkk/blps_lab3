package com.blps.lab2.services;

import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.googleplay.AppUser;
import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.MonetizationType;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.repo.googleplay.AppRepository;
import com.blps.lab2.repo.payments.PaymentRepository;
import com.blps.lab2.repo.googleplay.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();


    public Payment payForApp(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (!app.isNotFree()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This app is free. No payment required.");
        }

        Payment payment = processPayment(user, app, app.getAppPrice(), MonetizationType.FOR_MONEY);

        return payment;
    }

    public Payment payForInAppPurchase(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (!app.isInAppPurchases()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This app does not have in-app purchases.");
        }

        Payment payment = processPayment(user, app, app.getAppPrice(), MonetizationType.IN_APP_PURCHASES);
        return payment;
    }

    private Payment processPayment(AppUser user, App app, double amount, MonetizationType type) {
        Payment payment = new Payment();

        if (random.nextDouble() < 0.6) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment failed due to incorrect input data. Please try again later.");
        }

        if (random.nextDouble() < 0.1) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment failed due to a technical error. Please try again later.");
        }

        if (user.getBalance() < amount) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment failed due to insufficient funds.");
        }

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        app.setRevenue(app.getRevenue() + amount);
        app.getDeveloper().setEarnings(app.getDeveloper().getEarnings() + amount);
        appRepository.save(app);

        payment.setDeveloperId(app.getDeveloper().getId());
        payment.setAppId(app.getId());
        payment.setAmount(amount);
        payment.setMonetizationType(type);
        payment.setStatus(PaymentStatus.SUCCESS);

        return paymentRepository.save(payment);
    }

}