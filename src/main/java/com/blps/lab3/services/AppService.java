package com.blps.lab3.services;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.blps.lab3.dto.AppDto;
import com.blps.lab3.entities.googleplay.App;
import com.blps.lab3.entities.payments.Payment;
import com.blps.lab3.repo.googleplay.AppRepository;
import com.blps.lab3.repo.payments.PaymentRepository;
import jakarta.transaction.SystemException;
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

            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("App not found"));

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
            } catch (SystemException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }


    public AppDto getAppInfo(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        return AppDto.builder()
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
                .build();
    }

}
