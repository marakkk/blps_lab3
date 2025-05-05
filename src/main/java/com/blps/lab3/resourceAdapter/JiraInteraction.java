package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JiraInteraction implements Interaction {
    private final JiraConnection connection;
    private final RestTemplate restTemplate;
    private final HttpHeaders authHeaders;
    private final String jiraUrl;
    private final String jiraProjectKey;
    private final String defaultAssignee;

    private static final Logger logger = LoggerFactory.getLogger(JiraInteraction.class);


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
    public ResourceWarning getWarnings() throws ResourceException {
        return null;
    }

    @Override
    public void clearWarnings() throws ResourceException {

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
            String issueId = createManualReviewTask(input.getAppName(), input.getAppId());
            response.setIssueId(issueId);
            response.setStatus("CREATED");
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            response.setStatus("ERROR");
        }
        return response;
    }

    private JiraResponseRecord handleUpdateTaskStatus(JiraStatusUpdateRecord input) {
        JiraResponseRecord response = new JiraResponseRecord();
        try {
            updateTaskStatus(input.getIssueId(), input.getStatus());
            response.setIssueId(input.getIssueId());
            response.setStatus("UPDATED");
        } catch (Exception e) {
            logger.error("Failed to update status", e);
            response.setStatus("ERROR");
        }
        return response;
    }

    //TODO: why is the response null
    private String createManualReviewTask(String appName, Long appId) {
        String endpoint = jiraUrl + "/rest/api/2/issue";

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", jiraProjectKey));
        fields.put("summary", "Manual review required for app: " + appName);
        fields.put("description", "App ID: " + appId + "\nRequires manual review.");
        fields.put("issuetype", Map.of("name", "Task"));
        fields.put("assignee", Map.of("name", defaultAssignee));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("fields", fields),
                authHeaders
        );

        try {
            var response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<JiraRestIssueIdResponse>>() {}
            );
            logger.info("Response: {}", response.getBody());
            return response.getBody().toString();
        } catch (Exception e) {
            logger.error("Failed to create Jira issue", e);
            throw new RuntimeException("Failed to create Jira issue: " + e.getMessage(), e);
        }
    }

    private void updateTaskStatus(String issueId, String status) {
        String transitionId = getTransitionIdForStatus(issueId, status);
        if (transitionId == null) {
            throw new RuntimeException("No transition found for status: " + status);
        }

        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("transition", Map.of("id", transitionId)),
                authHeaders
        );

        try {
            restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Jira issue status: " + e.getMessage(), e);
        }
    }

    private String getTransitionIdForStatus(String issueId, String status) {
        List<JiraRestIssueIdResponse> transitions = getTransitionsForIssue(issueId);
        return transitions.stream().filter(el -> el.getName().equals(status)).findFirst().get().getId();
    }

    private List<JiraRestIssueIdResponse> getTransitionsForIssue(String issueId) {
        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";
        HttpEntity<String> request = new HttpEntity<>(authHeaders);

        try {
            var response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<JiraRestIssueIdResponse>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transitions for issue", e);
        }
    }
}
