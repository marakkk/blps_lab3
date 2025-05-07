package com.blps.lab3.services;

import com.blps.lab3.async.PaymentMessage;
import com.blps.lab3.async.StompMessageSender;
import com.blps.lab3.entities.googleplay.App;
import com.blps.lab3.entities.googleplay.AppUser;
import com.blps.lab3.entities.payments.Payment;
import com.blps.lab3.enums.MonetizationType;
import com.blps.lab3.enums.PaymentStatus;
import com.blps.lab3.repo.googleplay.AppRepository;
import com.blps.lab3.repo.payments.PaymentRepository;
import com.blps.lab3.repo.googleplay.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final JmsTemplate jmsTemplate;
    private final Random random = new Random();
    private final StompMessageSender stompMessageSender;


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
    public boolean hasSuccessfulPayment(Long userId, Long appId) {
        return paymentRepository.existsByAppIdAndUserIdAndStatus(appId, userId, PaymentStatus.SUCCESS);
    }

    private Payment processPayment(AppUser user, App app, double amount, MonetizationType type) {
        Payment payment = new Payment();
        payment.setUserId(user.getId());
        payment.setDeveloperId(app.getDeveloper().getId());
        payment.setAppId(app.getId());
        payment.setAmount(amount);
        payment.setMonetizationType(type);


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
        appRepository.save(app);

        payment.setStatus(PaymentStatus.SUCCESS);
        return paymentRepository.save(payment);
    }

    public String initiatePaidAppPurchase(Long userId, Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (!app.isNotFree()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This app is free");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        jmsTemplate.convertAndSend("app.payment.queue", new PaymentMessage(userId, appId));

        return "Payment process started for app: " + app.getName();
    }


}
