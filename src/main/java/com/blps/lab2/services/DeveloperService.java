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
        Payment payment = Payment.builder()
                .amount(PUBLISHING_FEE)
                .monetizationType(monetizationType)
                .developerId(developerId)
                .appId(appId)
                .build();
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

            App app = appRepository.findById(appId).orElseThrow();

            Developer developer = repository.findById(developerId)
                    .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + app.getDeveloper().getId()));

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
        return DeveloperDto.builder()
                .id(developer.getId())
                .name(developer.getUsername())
                .email(developer.getEmail())
                .paymentProfile(developer.isPaymentProfile())
                .accStatus(developer.getAccStatus())
                .earnings(developer.getEarnings())
                .apps(developer.getApps().stream()
                        .map(app -> AppDto.builder()
                                .id(app.getId())
                                .name(app.getName())
                                .version(app.getVersion())
                                .status(app.getStatus())
                                .downloads(app.getDownloads())
                                .revenue(app.getRevenue())
                                .inAppPurchases(app.isInAppPurchases())
                                .isNotFree(app.isNotFree())
                                .appPrice(app.getAppPrice())
                                .monetizationType(app.getMonetizationType())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}


