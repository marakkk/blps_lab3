package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class JiraInteraction implements Interaction {
    private final JiraConnection connection;
    private final RestTemplate restTemplate;
    private final HttpHeaders authHeaders;

    private static final Logger logger = LoggerFactory.getLogger(JiraInteraction.class);

    private ResourceWarning warnings;

    public Record execute(Record input) throws ResourceException {
        if (input == null || input.getRecordName() == null) {
            throw new ResourceException("Input record or its name cannot be null");
        }

        String operation = input.getRecordName();

        return switch (operation) {
            case "createTask" -> handleCreateTask((JiraRequestRecord) input);
            case "updateStatus" -> handleUpdateTaskStatus((JiraStatusUpdateRecord) input);
            default -> throw new ResourceException("Unsupported operation: " + operation);
        };
    }

    @Override
    public boolean execute(jakarta.resource.cci.InteractionSpec interactionSpec, Record input, Record output) {
        throw new UnsupportedOperationException("Use execute(Record) method instead.");
    }

    @Override
    public Record execute(InteractionSpec interactionSpec, Record record) throws ResourceException {
        return null;
    }

    @Override
    public ResourceWarning getWarnings() {
        return warnings;
    }

    @Override
    public void clearWarnings() {
        warnings = null;
    }

    @Override
    public void close() {
    }

    @Override
    public JiraConnection getConnection() {
        return connection;
    }

    private JiraResponseRecord handleCreateTask(JiraRequestRecord input) {
        JiraResponseRecord response = new JiraResponseRecord();
        try {
            String issueId = connection.createManualReviewTask(input.getAppName(), input.getAppId());
            response.setIssueId(issueId);
            response.setStatus("CREATED");
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            response.setStatus("ERROR");
            warnings = new ResourceWarning(e.getMessage());
        }
        return response;
    }

    private JiraResponseRecord handleUpdateTaskStatus(JiraStatusUpdateRecord input) {
        JiraResponseRecord response = new JiraResponseRecord();
        try {
            connection.updateTaskStatus(input.getIssueId(), input.getStatus());
            response.setIssueId(input.getIssueId());
            response.setStatus("UPDATED");
        } catch (Exception e) {
            logger.error("Failed to update status", e);
            response.setStatus("ERROR");
            warnings = new ResourceWarning(e.getMessage());
        }
        return response;
    }
}
