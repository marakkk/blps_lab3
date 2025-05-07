package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JiraInteraction implements Interaction {
    private final JiraConnection connection;
    private final RestTemplate restTemplate;
    private final HttpHeaders adminHeaders;
    private final HttpHeaders moderatorHeaders;
    private final String jiraUrl;
    private final String jiraProjectKey;
    private final String assignee;

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
            JiraRestIssueIdResponse createdIssue = createManualReviewTask(input.getAppName(), input.getAppId());
            response.setIssueId(createdIssue.getId());
            response.setIssueKey(createdIssue.getKey());
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

    private JiraRestIssueIdResponse createManualReviewTask(String appName, Long appId) {
        String endpoint = jiraUrl + "/rest/api/2/issue";

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", jiraProjectKey));
        fields.put("summary", "Manual review required for app: " + appName);
        fields.put("description", "App ID: " + appId + "\nRequires manual review.");
        fields.put("issuetype", Map.of("name", "Task"));
        fields.put("assignee", Map.of("name", assignee));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("fields", fields),
                adminHeaders
        );

        try {
            ResponseEntity<JiraRestIssueIdResponse> response = restTemplate.postForEntity(
                    endpoint,
                    request,
                    JiraRestIssueIdResponse.class
            );

            JiraRestIssueIdResponse body = response.getBody();
            if (body == null || body.getKey() == null) {
                throw new RuntimeException("Invalid response from Jira");
            }

            logger.info("Created jira task: {} with key: {}", body.getId(), body.getKey());
            return body;
        } catch (Exception e) {
            logger.error("Failed to create Jira issue", e);
            throw new RuntimeException("Failed to create Jira issue: " + e.getMessage(), e);
        }
    }

    private void updateTaskStatus(String issueId, String status) {
        String transitionId = getTransitionIdForStatus(issueId, status, adminHeaders);
        if (transitionId == null) {
            throw new RuntimeException("No transition found for status: " + status);
        }

        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";
        Map<String, Object> payload = Map.of("transition", Map.of("id", transitionId));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, moderatorHeaders);

        try {
            restTemplate.postForEntity(endpoint, request, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Jira issue status: " + e.getMessage(), e);
        }
    }

    private String getTransitionIdForStatus(String issueId, String status, HttpHeaders headers) {
        List<JiraTransitionsResponse.Transition> transitions = getTransitionsForIssue(issueId, headers);
        return findTransitionIdByStatus(transitions, status);
    }

    private List<JiraTransitionsResponse.Transition> getTransitionsForIssue(String issueId, HttpHeaders headers) {
        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JiraTransitionsResponse> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    request,
                    JiraTransitionsResponse.class
            );

            if (response.getBody() == null || response.getBody().getTransitions() == null) {
                throw new RuntimeException("Invalid transitions response from JIRA");
            }

            return response.getBody().getTransitions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transitions for issue: " + issueId, e);
        }
    }

    private String findTransitionIdByStatus(List<JiraTransitionsResponse.Transition> transitions, String status) {
        for (JiraTransitionsResponse.Transition transition : transitions) {
            String transitionName = transition.getName();
            logger.info("Transition found: {}", transitionName);
            if (transitionName.equalsIgnoreCase(status)) {
                return transition.getId();
            }
        }
        return null;
    }

}
