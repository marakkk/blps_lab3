package com.blps.lab2.jca;

import org.apache.log4j.Logger;

public class JiraConnectionManager {

    private JiraConnection connection;

    private static final Logger logger = Logger.getLogger(JiraConnectionManager.class.getName());

    public void connect() {
        try {
            connection = new JiraConnection();
            //что-то для подключения к джира?
        } catch (Exception e) {
            logger.error("Failed to connect to Jira: " + e.getMessage());
            throw new RuntimeException("Error connecting to Jira", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            connection.close();
            logger.error("Disconnected from Jira.");
        }
    }
}
