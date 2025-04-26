package com.blps.lab2.services;

import com.atomikos.icatch.jta.TransactionManagerImp;
import com.blps.lab2.dto.AppDto;
import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.repo.googleplay.AppRepository;
import com.blps.lab2.repo.payments.PaymentRepository;
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
    private final UserTransaction userTransaction;
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private String getTransactionId() {
        try {
            var transaction = TransactionManagerImp.getTransactionManager().getTransaction();
            return transaction != null ? transaction.toString() : "NO_ACTIVE_TRANSACTION";
        } catch (Exception e) {
            logger.warn("Failed to retrieve transaction ID.");
            return "UNKNOWN";
        }
    }


    public App createApp(AppDto appDto) {
        try {
            userTransaction.begin();

            String transactionId = getTransactionId();
            logger.info("[TX:{}] Creating new app: {}", transactionId, appDto.getName());

            App app = new App();
            app.setName(appDto.getName());
            app.setVersion(appDto.getVersion());
            app.setStatus(AppStatus.PENDING);
            app.setDownloads(0);
            app.setRevenue(0);
            app.setInAppPurchases(appDto.isInAppPurchases());
            app.setNotFree(appDto.isNotFree());
            app.setAppPrice(appDto.getAppPrice());
            app.setMonetizationType(appDto.getMonetizationType());

            app = appRepository.save(app);
            userTransaction.commit();

            logger.info("[TX:{}] App created successfully with ID: {}", transactionId, app.getId());
            return app;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to create app: " + e.getMessage(), e);
        }
    }

    public App updateApp(Long appId, AppDto appDto) {
        try {
            userTransaction.begin();

            String transactionId = getTransactionId();
            logger.info("[TX:{}] Updating app with ID: {}", transactionId, appId);

            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

            app.setName(appDto.getName());
            app.setVersion(appDto.getVersion());
            app.setInAppPurchases(appDto.isInAppPurchases());
            app.setNotFree(appDto.isNotFree());
            app.setAppPrice(appDto.getAppPrice());
            app.setMonetizationType(appDto.getMonetizationType());

            app = appRepository.save(app);
            userTransaction.commit();

            logger.info("[TX:{}] App updated successfully: {}", transactionId, app);
            return app;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to update app: " + e.getMessage(), e);
        }
    }

    public void deleteApp(Long appId) {
        try {

            userTransaction.begin();
            String transactionId = getTransactionId();
            logger.info("[TX:{}] Attempting to delete app with ID: {}", transactionId, appId);

            if (!appRepository.existsById(appId)) {
                throw new IllegalArgumentException("App with ID " + appId + " not found. Cannot delete.");
            }

            appRepository.deleteById(appId);
            userTransaction.commit();

            logger.info("[TX:{}] App deleted successfully.", transactionId);
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to delete app: " + e.getMessage(), e);
        }
    }

    public Payment createPayment(Long appId, double amount) {
        try {
            userTransaction.begin();
            String transactionId = getTransactionId();
            logger.info("[TX:{}] Creating payment for App ID: {}", transactionId, appId);

            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

            Payment payment = new Payment();
            payment.setAppId(appId);
            payment.setAmount(amount);
            payment.setStatus(PaymentStatus.SUCCESS);

            paymentRepository.save(payment);
            userTransaction.commit();

            logger.info("[TX:{}] Payment created successfully: {}", transactionId, payment);
            return payment;
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public Map<String, String> moderateApp(Long appId, boolean approved, String moderatorComment) {
        try {
            userTransaction.begin();
            String transactionId = getTransactionId();
            logger.info("[TX:{}] Moderating app ID: {} | Approved: {}", transactionId, appId, approved);

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

            logger.info("[TX:{}] App moderation complete: {}", transactionId, response);
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
