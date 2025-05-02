package com.blps.lab2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    @Value("${spring.mail.username}")
    private String moderatorEmail;

    private final JavaMailSender mailSender;

    public MailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyModeratorOfNewTask(String appName, Long appId, String jiraIssueId) {
        String subject = "Новая задача на ревью в Jira";
        String body = String.format(
                "Приложение \"%s\" (ID: %d) требует ручной проверки.\n\n" +
                        "Ссылка на задачу: http://localhost:3180/browse/%s\n\n" +
                        "Зайдите в Jira и начните выполнение.",
                appName, appId, jiraIssueId
        );

        sendEmail(moderatorEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

}
