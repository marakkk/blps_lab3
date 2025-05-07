package com.blps.lab3.controllers;

import com.blps.lab3.dto.AppDto;
import com.blps.lab3.entities.payments.Payment;
import com.blps.lab3.services.AppUserService;
import com.blps.lab3.services.PaymentService;
import com.blps.lab3.dto.AppDto;
import com.blps.lab3.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-actions")
public class AppUserController {

    private final AppUserService appUserService;
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('USER') and hasAuthority('APP_CATALOG')")
    @GetMapping("/catalog")
    public ResponseEntity<List<AppDto>> viewAppCatalog() {
        List<AppDto> catalog = appUserService.viewAppCatalog();
        return ResponseEntity.ok(catalog);
    }

    @PreAuthorize("hasRole('USER') and hasAuthority('APP_DOWNLOAD')")
    @PostMapping("/{userId}/download/{appId}")
    public ResponseEntity<String> downloadApp(@PathVariable Long userId, @PathVariable Long appId) {
        String result = appUserService.downloadApp(userId, appId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('USER') and hasAuthority('APP_USE')")
    @PostMapping("/{userId}/use/{appId}")
    public ResponseEntity<String> useApp(@PathVariable Long userId, @PathVariable Long appId) {
        String result = appUserService.useApp(userId, appId);
        return ResponseEntity.ok(result);
    }


    @PreAuthorize("hasRole('USER') and hasAuthority('APP_PURCHASE')")
    @PostMapping("/{userId}/purchase/{appId}")
    public ResponseEntity<String> initiatePaidAppPurchase(@PathVariable Long userId, @PathVariable Long appId) {
        String result = paymentService.initiatePaidAppPurchase(userId, appId);
        return ResponseEntity.accepted().body(result);
    }
}
