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
    private final StompMessageSender stompMessageSender;

    @JmsListener(destination = "app.download.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleDownloadMessage(DownloadMessage message) {
       // Thread.sleep(5000);
        try {
            String result = appUserService.completePaidAppDownload(message.getUserId(), message.getAppId());
            stompMessageSender.send("/queue/app.download.success.queue",
                    result);
        } catch (Exception e) {
            stompMessageSender.send("/queue/app.download.error.queue",
                    new ErrorMessage(message.getUserId(), message.getAppId(), e.getMessage()));
            throw e;
        }
    }
}
