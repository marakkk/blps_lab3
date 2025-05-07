package com.blps.lab3.async;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class StompMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(StompMessageSender.class);

    private final WebSocketStompClient stompClient;
    private final StompSessionHandler sessionHandler;
    private volatile StompSession stompSession;
    private final CompletableFuture<Void> connectionFuture = new CompletableFuture<>();

    @Value("${stomp.endpoint}")
    private String stompEndpoint;

    @Value("${stomp.username}")
    private String stompUsername;

    @Value("${stomp.password}")
    private String stompPassword;

    @Autowired
    public StompMessageSender(WebSocketStompClient stompClient, StompSessionHandler sessionHandler) {
        this.stompClient = stompClient;
        this.sessionHandler = sessionHandler;
    }

    @PostConstruct
    public void init() {
        logger.info("STOMP Endpoint: {}", stompEndpoint);
        logger.info("STOMP Username: {}", stompUsername);
        initializeConnection();
    }

    private void initializeConnection() {
        logger.info("Attempting connection to: {}", stompEndpoint);

        stompClient.connectAsync(stompEndpoint, sessionHandler)
                .orTimeout(30, TimeUnit.SECONDS)
                .thenAccept(session -> {
                    logger.info("Successfully connected to {}", stompEndpoint);
                    stompSession = session;
                    connectionFuture.complete(null);
                })
                .exceptionally(error -> {
                    logger.error("Connection failed", error);
                    scheduleReconnect();
                    return null;
                });
    }


    private void scheduleReconnect() {
        try {
            logger.warn("Connection failed. Retrying in 5 seconds...");
            TimeUnit.SECONDS.sleep(5);
            initializeConnection();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("Reconnect thread interrupted", ie);
        }
    }

    public boolean isConnected() {
        return stompSession != null && stompSession.isConnected();
    }

    public void send(String destination, Object payload) {
        try {
            connectionFuture.get(10, TimeUnit.SECONDS);

            if (!isConnected()) {
                throw new IllegalStateException("STOMP session not connected");
            }

            byte[] bytePayload;
            if (payload instanceof String) {
                bytePayload = ((String) payload).getBytes();
            } else if (payload instanceof byte[]) {
                bytePayload = (byte[]) payload;
            } else {
                throw new IllegalArgumentException("Payload must be String or byte[]");
            }

            logger.debug("Sending message to {}: {}", destination, payload);
            stompSession.send(destination, bytePayload);

        } catch (TimeoutException e) {
            throw new IllegalStateException("Timeout waiting for STOMP connection", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send STOMP message to " + destination, e);
        }
    }

}