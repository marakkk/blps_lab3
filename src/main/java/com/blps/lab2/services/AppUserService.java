package com.blps.lab2.services;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.blps.lab2.async.PaymentMessage;
import com.blps.lab2.dto.AppDto;
import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.googleplay.AppUser;
import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.repo.googleplay.AppRepository;
import com.blps.lab2.repo.googleplay.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final PaymentService paymentService;
    private final UserTransactionManager userTransaction;
    private final JmsTemplate jmsTemplate;


    public List<AppDto> viewAppCatalog() {
        return appRepository.findAll().stream()
                .map(app -> new AppDto(
                        app.getId(),
                        app.getName(),
                        app.getVersion(),
                        app.getStatus(),
                        app.getDownloads(),
                        app.getRevenue(),
                        app.isInAppPurchases(),
                        app.isNotFree(),
                        app.getAppPrice(),
                        app.getMonetizationType()
                ))
                .collect(Collectors.toList());
    }

    //download provjerat statusi oplaecno li eto priloyenije etim juzerom
    public String downloadApp(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (app.isNotFree()) {
            // Check if payment exists and was successful
            boolean hasSuccessfulPayment = paymentService.hasSuccessfulPayment(userId, appId);
            if (!hasSuccessfulPayment) {
                throw new IllegalArgumentException("Payment required before downloading this app");
            }
        }

        return "User " + user.getUsername() + " successfully downloaded " + app.getName() + ".";
    }


    public String useApp(Long userId, Long appId) {
        try {
            userTransaction.begin();

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

            String result = "User " + user.getUsername() + " started using " + app.getName() + ".";

            if (app.isInAppPurchases()) {
                result += "\nIn-app purchases detected.";
                Payment inAppPayment = paymentService.payForInAppPurchase(userId, appId);
                if (inAppPayment.getStatus() == PaymentStatus.SUCCESS) {
                    result += "\nUser purchased in-app content and continues using the app.";
                } else {
                    result += "\nUser could not purchase in-app content.";
                }
            }

            userTransaction.commit();
            return result;
        } catch (Exception e) {
            if (userTransaction != null) {
                try {
                    userTransaction.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }


    public String initiatePaidAppPurchase(Long userId, Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (!app.isNotFree()) {
            throw new IllegalArgumentException("This app is free");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        jmsTemplate.convertAndSend("app.payment.queue", new PaymentMessage(userId, appId));

        return "Payment process started for app: " + app.getName();
    }

    @Transactional
    public String completePaidAppDownload(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        // Verify payment was successful
        boolean hasSuccessfulPayment = paymentService.hasSuccessfulPayment(userId, appId);
        if (!hasSuccessfulPayment) {
            throw new IllegalStateException("Payment verification failed");
        }

        return "User " + user.getUsername() + " successfully downloaded paid app: " + app.getName();
    }
}
