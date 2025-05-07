package com.blps.lab3.scheduler;

import com.blps.lab3.entities.googleplay.App;
import com.blps.lab3.repo.googleplay.AppRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final AppRepository appRepository;
    private static final Logger logger = LoggerFactory.getLogger(PayoutService.class);

    @Transactional
    public void processDeveloperPayouts() {
        List<App> apps = appRepository.findAll();
        for (App app : apps) {
            double earnings = calculateEarnings(app);
            app.getDeveloper().setEarnings(app.getDeveloper().getEarnings() + earnings);
            logger.info("Added ${} to developer {} from app {}", earnings, app.getDeveloper().getUsername(), app.getName());
        }
    }

    private double calculateEarnings(App app) {
        if (app.isNotFree()) {
            logger.info("App is not free so some money is added to developer {} from app {}", app.getDeveloper().getUsername(), app.getName());
            return app.getAppPrice() * app.getDownloads();
        } else if (app.isInAppPurchases()) {
            logger.info("App is free so revenue {} added to developer {} from app {}", app.getRevenue(), app.getDeveloper().getUsername(), app.getName());
            return app.getRevenue();
        } else {
            logger.info("Nothing is added to developer {} from app {}", app.getDeveloper().getUsername(), app.getName());
            return 0.0;
        }
    }
}
