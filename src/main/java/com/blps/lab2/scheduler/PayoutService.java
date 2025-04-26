package com.blps.lab2.scheduler;

import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.repo.googleplay.AppRepository;
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
        // нужно тут довести до ума логику
        return 10.0;
    }
}
