package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class JiraConnection implements Connection {

    @Getter
    private boolean closed = false;

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.username}")
    private String jiraUsername;

    @Value("${jira.token}")
    private String jiraApiToken;

    @Value("${jira.projectKey}")
    private String jiraProjectKey;

    @Value("${jira.assignee}")
    private String defaultAssignee;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(JiraConnection.class);

    @Override
    public Interaction createInteraction() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    @Override
    public void close() {
        closed = true;
    }

    public String createManualReviewTask(String appName, Long appId) {
        String endpoint = jiraUrl + "/rest/api/2/issue";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = jiraUsername + ":" + jiraApiToken;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", jiraProjectKey));
        fields.put("summary", "Manual review required for app: " + appName);
        fields.put("description", "App ID: " + appId + "\nRequires manual review.");
        fields.put("issuetype", Map.of("name", "Task"));
        fields.put("assignee", Map.of("name", defaultAssignee));

        Map<String, Object> payload = Map.of("fields", fields);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            logger.info("Response: {}", response.getBody());
            return extractIssueIdFromResponse(response.getBody());
        } catch (Exception e) {
            logger.info("Error: {}", e.getMessage());
            throw new RuntimeException("Failed to create Jira issue: " + e.getMessage(), e);
        }

    }

    private HttpHeaders createAuthHeaders(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = username + ":" + token;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        return headers;
    }


    public void updateTaskStatus(String issueId, String status, String username, String token) {
        String transitionId = getTransitionIdForStatus(issueId, status, username, token);
        if (transitionId == null) {
            throw new RuntimeException("No transition found for status: " + status);
        }

        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";

        HttpHeaders headers = createAuthHeaders(username, token);

        Map<String, Object> payload = Map.of("transition", Map.of("id", transitionId));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Jira issue status: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> parseTransitionsResponse(String responseBody) {
        List<Map<String, Object>> transitions = new ArrayList<>();

        int startIdx = responseBody.indexOf("[");
        int endIdx = responseBody.indexOf("]");

        if (startIdx != -1 && endIdx != -1) {
            String transitionsArray = responseBody.substring(startIdx + 1, endIdx).trim();

            String[] transitionStrings = transitionsArray.split("\\},\\{");
            for (String transitionString : transitionStrings) {
                Map<String, Object> transition = new HashMap<>();

                String id = extractJsonFieldValue(transitionString, "id");
                transition.put("id", id);

                String name = extractJsonFieldValue(transitionString, "name");
                transition.put("name", name);

                transitions.add(transition);
            }
        }

        return transitions;
    }

    private String extractJsonFieldValue(String jsonString, String fieldName) {
        String fieldMarker = "\"" + fieldName + "\":\"";
        int fieldStartIdx = jsonString.indexOf(fieldMarker);

        if (fieldStartIdx != -1) {
            int fieldEndIdx = jsonString.indexOf("\"", fieldStartIdx + fieldMarker.length());
            if (fieldEndIdx != -1) {
                return jsonString.substring(fieldStartIdx + fieldMarker.length(), fieldEndIdx);
            }
        }
        return "";
    }

    private String extractIssueIdFromResponse(String response) {
        return extractJsonFieldValue(response, "id");
    }

    private List<Map<String, Object>> getTransitionsForIssue(String issueId, String username, String token) {
        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";

        HttpHeaders headers = createAuthHeaders(username, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
            logger.info("Transitions response: {}", response.getBody());
            return parseTransitionsResponse(Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transitions for issue: " + e.getMessage(), e);
        }
    }

    private String getTransitionIdForStatus(String issueId, String status, String username, String token) {
        List<Map<String, Object>> transitions = getTransitionsForIssue(issueId, username, token);
        return findTransitionIdByStatus(transitions, status);
    }

    private String findTransitionIdByStatus(List<Map<String, Object>> transitions, String status) {
        for (Map<String, Object> transition : transitions) {
            String transitionName = (String) transition.get("name");
            logger.info("Transition found: {}", transitionName);
            if (transitionName.equalsIgnoreCase(status)) {
                return (String) transition.get("id");
            }
        }
        return null;
    }

}
