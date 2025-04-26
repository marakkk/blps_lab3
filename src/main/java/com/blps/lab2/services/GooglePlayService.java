package com.blps.lab2.services;

import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.enums.AppStatus;
import com.blps.lab2.repo.googleplay.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class GooglePlayService {

    private final AppRepository appRepository;
    private final Random random = new Random();

    public Map<String, String> autoReviewApp(App app) {

        if (app.getStatus() != AppStatus.PENDING) {
            throw new IllegalStateException("App must be submitted for review before auto-review can start.");
        }

        Map<String, String> response = new HashMap<>();
        int totalSeverity = 0;

        Map<String, Integer> technicalIssues = checkTechnicalIssues(app);
        for (Integer severity : technicalIssues.values()) {
            totalSeverity += severity;
        }

        if (totalSeverity > 15) {
            return rejectApp(app, "App has critical issues: " + technicalIssues.keySet());
        }

        Map<String, Integer> policyIssues = checkPolicyCompliance(app);
        for (Integer severity : policyIssues.values()) {
            totalSeverity += severity;
        }

        if (totalSeverity > 10) {
            return rejectApp(app, "App violates Google Play policies: " + policyIssues.keySet());
        }

        double manualReviewChance = Math.min(0.3 + (totalSeverity * 0.05), 0.9);

        if (random.nextDouble() < manualReviewChance) {
            app.setStatus(AppStatus.UNDER_REVIEW);
            response.put("message", "App requires manual review.");
        } else {
            app.setStatus(AppStatus.APPROVED);
            response.put("message", "App approved automatically.");
        }

        appRepository.save(app);
        return response;

    }

    public Map<String, String> manualReviewApp(App app, boolean approved, String moderatorComment) {
        if (app.getStatus() != AppStatus.UNDER_REVIEW) {
            throw new IllegalStateException("App must be in UNDER_REVIEW status for manual review.");
        }

        Map<String, String> response = new HashMap<>();
        if (approved) {
            app.setStatus(AppStatus.APPROVED);
            response.put("message", "App approved by moderator.");
        } else {
            app.setStatus(AppStatus.REJECTED);
            response.put("reason", moderatorComment);
        }

        appRepository.save(app);
        return response;

    }

    public void publishApp(App app) {
        if (app.getStatus() != AppStatus.APPROVED) {
            throw new IllegalStateException("App must be approved before publishing.");
        }

        app.setStatus(AppStatus.PUBLISHED);
        appRepository.save(app);
    }

    private Map<String, String> rejectApp(App app, String reason) {
        app.setStatus(AppStatus.REJECTED);
        appRepository.save(app);
        return Map.of("reason", reason);
    }

    private Map<String, Integer> checkTechnicalIssues(App app) {
        Map<String, Integer> issues = new HashMap<>();
        if (!app.isCorrectPermissions()) {
            issues.put("Permissions are incorrect or excessive.", random.nextInt(5) + 3);
        }
        if (!app.isCorrectMetadata()) {
            issues.put("App metadata is incorrect or incomplete.", random.nextInt(4) + 2);
        }
        return issues;
    }

    private Map<String, Integer> checkPolicyCompliance(App app) {
        Map<String, Integer> issues = new HashMap<>();
        if (app.isViolatesGooglePlayPolicies()) {
            issues.put("App violates Google Play policies.", random.nextInt(6) + 5);
        }
        if (app.isChildrenBadPolicy()) {
            issues.put("App does not comply with children's content policies.", random.nextInt(6) + 5);
        }
        return issues;
    }
}
