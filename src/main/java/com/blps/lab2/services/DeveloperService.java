package com.blps.lab2.services;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.blps.lab2.dto.AppDto;
import com.blps.lab2.dto.DeveloperDto;
import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.googleplay.Developer;
import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.enums.DevAccount;
import com.blps.lab2.enums.MonetizationType;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.repo.googleplay.AppRepository;
import com.blps.lab2.repo.googleplay.DeveloperRepository;
import com.blps.lab2.repo.payments.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DeveloperService {

    private static final double PUBLISHING_FEE = 25.0;
    private static final Logger logger = LoggerFactory.getLogger(DeveloperService.class);

    private final DeveloperRepository repository;
    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;
    private final GooglePlayService googlePlayService;
    private final UserTransactionManager userTransaction;


    private boolean isValidVersion(double version) {
        return version > 0;
    }

    private Payment createPaymentForAppSubmission(Long developerId, Long appId, MonetizationType monetizationType) {
        Payment payment = new Payment();
        payment.setAmount(PUBLISHING_FEE);
        payment.setMonetizationType(monetizationType);
        payment.setDeveloperId(developerId);
        payment.setAppId(appId);
        paymentRepository.save(payment);
        return payment;
    }

    private boolean processPayment(Payment payment) {
        if (payment.getAmount() > 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            return true;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return false;
        }
    }

    private void notifyDeveloperPaymentSuccess(Developer developer, Payment payment) {
        logger.info("Developer {} has successfully paid the fee: {}", developer.getUsername(), payment.getAmount());
    }

    private MonetizationType determineMonetizationType(boolean wantsToMonetize, boolean wantsToCharge) {
        if (!wantsToMonetize && !wantsToCharge) {
            return MonetizationType.FREE;
        } else if (wantsToCharge) {
            return MonetizationType.FOR_MONEY;
        } else {
            return MonetizationType.IN_APP_PURCHASES;
        }
    }

    public void register(Long developerId) {
        Developer dev = repository.findById(developerId).orElseThrow();
        dev.setAccStatus(DevAccount.PAID);
        dev.setPaymentProfile(true);
        repository.save(dev);
    }

    public void validateApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));
        if (app.getName() == null || app.getName().isEmpty() || !isValidVersion(app.getVersion()) || !app.isCorrectPermissions()) {
            throw new IllegalStateException("App validation failed.");
        }
        app.setStatus(AppStatus.VALIDATED);
        appRepository.save(app);

    }

    public App submitApp(Long developerId, Long appId, boolean wantsToMonetize, boolean wantsToCharge) {
        try {
            userTransaction.begin();

            Developer developer = repository.findById(developerId).orElseThrow();
            if (developer.getAccStatus() == DevAccount.UNPAID) register(developerId);
            if (!developer.isPaymentProfile()) throw new IllegalStateException("Payment profile required");

            App app = appRepository.findById(appId).orElseThrow();
            validateApp(appId);

            MonetizationType monetizationType = determineMonetizationType(wantsToMonetize, wantsToCharge);
            app.setMonetizationType(monetizationType);

            Payment payment = createPaymentForAppSubmission(developerId, appId, monetizationType);
            if (!processPayment(payment)) throw new IllegalStateException("Payment failed");

            app.setStatus(AppStatus.PENDING);
            app.setDeveloper(developer);
            appRepository.save(app);

            notifyDeveloperPaymentSuccess(developer, payment);

            userTransaction.commit();
            return app;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }

    public Map<String, String> publishApp(Long developerId, Long appId, boolean approvedByModerator, String moderatorComment) {
        try {
            userTransaction.begin();

            Developer developer = repository.findById(developerId).orElseThrow();
            App app = appRepository.findById(appId).orElseThrow();

            if (!app.getDeveloper().equals(developer)) throw new IllegalStateException("Unauthorized");
            if (app.getStatus() != AppStatus.PENDING) throw new IllegalStateException("App must be pending");

            Map<String, String> reviewResult = googlePlayService.autoReviewApp(app);
            if ("App approved automatically.".equals(reviewResult.get("message"))) {
                googlePlayService.publishApp(app);
                userTransaction.commit();
                return Map.of("message", "Published automatically");
            }

            if ("App requires manual review.".equals(reviewResult.get("message"))) {
                Map<String, String> manualReviewResult = googlePlayService.manualReviewApp(app, approvedByModerator, moderatorComment);
                if (manualReviewResult.containsKey("reason")) {
                    userTransaction.rollback();
                    return Map.of("error", "Rejected", "reason", manualReviewResult.get("reason"));
                }

                googlePlayService.publishApp(app);
                userTransaction.commit();
                return Map.of("message", "Published after review");
            }

            userTransaction.rollback();
            return Map.of("error", "Unexpected issue");
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }

    public DeveloperDto getDeveloperInfo(Long developerId) {
        Developer developer = repository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found"));
        return new DeveloperDto(
                developer.getId(),
                developer.getUsername(),
                developer.getEmail(),
                developer.isPaymentProfile(),
                developer.getAccStatus(),
                developer.getEarnings(),
                developer.getApps().stream().map(app -> new AppDto(
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
                )).collect(Collectors.toList())
        );
    }
}


