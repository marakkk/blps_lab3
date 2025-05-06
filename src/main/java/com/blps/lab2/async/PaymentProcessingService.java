package com.blps.lab2.async;

import com.blps.lab2.entities.payments.Payment;
import com.blps.lab2.enums.PaymentStatus;
import com.blps.lab2.services.AppUserService;
import com.blps.lab2.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentService paymentService;
    private final JmsTemplate jmsTemplate;
    private final StompMessageSender stompMessageSender;

    @JmsListener(destination = "app.payment.queue")
    public void processPayment(PaymentMessage message) throws JsonProcessingException {
        try {
            Payment payment = paymentService.payForApp(message.getUserId(), message.getAppId());
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                jmsTemplate.convertAndSend("app.download.queue",
                        new DownloadMessage(message.getUserId(), message.getAppId()));
            }
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage(message.getUserId(), message.getAppId(), e.getMessage());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(errorMessage);

            stompMessageSender.send("/queue/app.payment.error.queue", json); // Send as String, not as object

        }

}
}
