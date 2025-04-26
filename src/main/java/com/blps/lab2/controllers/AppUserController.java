package com.blps.lab2.controllers;

import com.blps.lab2.dto.AppDto;
import com.blps.lab2.services.AppUserService;
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
}
