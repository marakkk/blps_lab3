package com.blps.lab3.services;

import com.blps.lab3.dto.AppDto;
import com.blps.lab3.entities.googleplay.App;
import com.blps.lab3.entities.googleplay.Developer;
import com.blps.lab3.entities.payments.Payment;
import com.blps.lab3.enums.AppStatus;
import com.blps.lab3.enums.PaymentStatus;
import com.blps.lab3.repo.googleplay.AppRepository;
import com.blps.lab3.repo.googleplay.DeveloperRepository;
import com.blps.lab3.repo.payments.PaymentRepository;
import jakarta.transaction.UserTransaction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;
    private final DeveloperRepository developerRepository;
    private final UserTransaction userTransaction;
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public App createApp(AppDto appDto) {
        try {
            userTransaction.begin();

            logger.info("Creating new app: {}", appDto.getName());

            Developer developer = developerRepository.findById(appDto.getDeveloperId())
                    .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + appDto.getDeveloperId()));

            App app = App.builder()
                    .name(appDto.getName())
                    .version(appDto.getVersion())
                    .status(AppStatus.PENDING)
                    .downloads(0)
                    .revenue(0)
                    .inAppPurchases(appDto.isInAppPurchases())
                    .isNotFree(appDto.isNotFree())
                    .appPrice(appDto.getAppPrice())
                    .monetizationType(appDto.getMonetizationType())
                    .correctPermissions(appDto.isCorrectPermissions())
                    .correctMetadata(appDto.isCorrectMetaData())
                    .isViolatesGooglePlayPolicies(appDto.isViolatesGooglePlayPolicies())
                    .isChildrenBadPolicy(appDto.isChildrenBadPolicy())
                    .developer(developer)
                    .build();

            app = appRepository.save(app);
            userTransaction.commit();

            logger.info("App created successfully with ID: {}", app.getId());
            return app;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to create app: " + e.getMessage(), e);
        }
    }

    public App updateApp(Long appId, AppDto appDto) {
        try {
            userTransaction.begin();

            logger.info("Updating app with ID: {}", appId);

            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

            Developer developer = developerRepository.findById(appDto.getDeveloperId())
                    .orElseThrow(() -> new IllegalArgumentException("Developer not found with ID: " + appDto.getDeveloperId()));

            app = app.toBuilder()
                    .name(appDto.getName())
                    .version(appDto.getVersion())
                    .inAppPurchases(appDto.isInAppPurchases())
                    .isNotFree(appDto.isNotFree())
                    .appPrice(appDto.getAppPrice())
                    .monetizationType(appDto.getMonetizationType())
                    .developer(developer)
                    .build();

            app = appRepository.save(app);
            userTransaction.commit();

            logger.info("App updated successfully: {}", app);
            return app;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to update app: " + e.getMessage(), e);
        }
    }

    public void deleteApp(Long appId) {
        try {

            userTransaction.begin();
            logger.info("Attempting to delete app with ID: {}", appId);

            if (!appRepository.existsById(appId)) {
                throw new IllegalArgumentException("App with ID " + appId + " not found. Cannot delete.");
            }

            appRepository.deleteById(appId);
            userTransaction.commit();

            logger.info("App deleted successfully.");
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to delete app: " + e.getMessage(), e);
        }
    }

    public Payment createPayment(Long appId, double amount) {
        try {
            userTransaction.begin();
            logger.info("Creating payment for App ID: {}", appId);

            Payment payment = Payment.builder()
                    .appId(appId)
                    .amount(amount)
                    .status(PaymentStatus.SUCCESS)
                    .build();

            paymentRepository.save(payment);
            userTransaction.commit();

            logger.info("Payment created successfully: {}", payment);
            return payment;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public Map<String, String> moderateApp(Long appId, boolean approved, String moderatorComment) {
        try {
            userTransaction.begin();
            logger.info("Moderating app ID: {} | Approved: {}", appId, approved);

            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

            Map<String, String> response = new HashMap<>();

            if (approved) {
                app.setStatus(AppStatus.APPROVED);
                response.put("message", "App approved by moderator.");
            } else {
                app.setStatus(AppStatus.REJECTED);
                response.put("reason", moderatorComment);
            }

            appRepository.save(app);
            userTransaction.commit();

            logger.info("App moderation complete: {}", response);
            return response;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to moderate app: " + e.getMessage(), e);
        }
    }

    private void rollbackTransaction() {
        try {
            userTransaction.rollback();
            logger.warn("Transaction rolled back.");
        } catch (Exception rollbackEx) {
            logger.error("Transaction rollback failed: {}", rollbackEx.getMessage());
        }
    }

}
