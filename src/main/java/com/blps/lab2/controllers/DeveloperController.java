package com.blps.lab2.controllers;

import com.blps.lab2.dto.AppDto;
import com.blps.lab2.dto.DeveloperDto;
import com.blps.lab2.entities.googleplay.App;
import com.blps.lab2.errors.ErrorResponse;
import com.blps.lab2.services.AppService;
import com.blps.lab2.services.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/developer-actions")
class DeveloperController {

    private final DeveloperService developerService;
    private final AppService appService;

    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('APP_SUBMIT')")
    @PostMapping("/{developerId}/apps/{appId}/submit")
    public ResponseEntity<Object> submitApp(
            @PathVariable Long developerId,
            @PathVariable Long appId,
            @RequestParam boolean wantsToMonetize,
            @RequestParam boolean wantsToCharge) {

        try {
            App submittedApp = developerService.submitApp(developerId, appId, wantsToMonetize, wantsToCharge);
            return ResponseEntity.ok(submittedApp);
        } catch (IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('APP_PUBLISH')")
    @PostMapping("/{developerId}/apps/{appId}/publish")
    public ResponseEntity<Map<String, String>> publishApp(@PathVariable Long developerId,
                                                          @PathVariable Long appId,
                                                          @RequestParam boolean approvedByModerator,
                                                          @RequestParam String moderatorComment) {
        Map<String, String> result = developerService.publishApp(developerId, appId, approvedByModerator, moderatorComment);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('DEV_INFO')")
    @GetMapping("/{id}/dev-info")
    public ResponseEntity<DeveloperDto> getDeveloperInfo(@PathVariable Long id) {
        DeveloperDto developer = developerService.getDeveloperInfo(id);
        return ResponseEntity.ok(developer);
    }

    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('APP_UPDATE')")
    @PutMapping("/{id}/analytics")
    public Map<String, Object> updateAnalytics(@PathVariable Long id) {
        return appService.updateAnalytics(id);
    }


    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('APP_INFO')")
    @GetMapping("/{id}/app-info")
    public ResponseEntity<AppDto> getAppInfo(@PathVariable Long id) {
        AppDto app = appService.getAppInfo(id);
        return ResponseEntity.ok(app);
    }

}
