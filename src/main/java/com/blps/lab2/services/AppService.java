package com.blps.lab2.services;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.blps.lab2.dto.AppDto;
import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.repo.googleplay.AppRepository;
import com.blps.lab2.repo.payments.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;
    private final UserTransactionManager userTransaction;

    public Map<String, Object> updateAnalytics(Long appId) {
        try {
            userTransaction.begin();

            App app = appRepository.findById(appId).orElseThrow();

            double spentByUsers = paymentRepository.findByAppId(appId)
                    .stream()
                    .mapToDouble(Payment::getAmount)
                    .sum();

            app.setRevenue(app.getRevenue() + spentByUsers);
            app.getDeveloper().setEarnings(app.getDeveloper().getEarnings() + spentByUsers);

            appRepository.save(app);

            userTransaction.commit();

            return Map.of(
                    "downloads", app.getDownloads(),
                    "revenue", app.getRevenue()
            );
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }


    public AppDto getAppInfo(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        return new AppDto(
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
        );
    }

}
