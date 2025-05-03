package com.blps.lab2.async;



import com.blps.lab2.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DownloadMessageListener {
    private final AppUserService appUserService;

    @JmsListener(destination = "app.download.queue")
    @Transactional
    public void handleDownloadMessage(DownloadMessage message) {
        String result = appUserService.completePaidAppDownload(message.getUserId(), message.getAppId());

    }
}
