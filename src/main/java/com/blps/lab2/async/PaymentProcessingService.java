package com.blps.lab2.async;

import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.services.AppUserService;
import com.blps.lab2.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentService paymentService;
    private final AppUserService appUserService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "app.payment.queue")
    @Transactional
    public void processPayment(PaymentMessage message) {
        try {
            Payment payment = paymentService.payForApp(message.getUserId(), message.getAppId());
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                jmsTemplate.convertAndSend("app.download.queue",
                        new DownloadMessage(message.getUserId(), message.getAppId()));
            }
        } catch (Exception e) {
            jmsTemplate.convertAndSend("app.payment.error.queue",
                    new ErrorMessage(message.getUserId(), message.getAppId(), e.getMessage()));
        }
    }
}
