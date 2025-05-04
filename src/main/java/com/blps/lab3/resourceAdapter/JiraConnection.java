package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@AllArgsConstructor
public class JiraConnection implements Connection, AutoCloseable {

    private final RestTemplate restTemplate;
    private final HttpHeaders authHeaders;
    private final String jiraUrl;
    private final String jiraProjectKey;
    private final String defaultAssignee;

    private static final Logger logger = LoggerFactory.getLogger(JiraConnection.class);

    @Override
    public Interaction createInteraction() throws ResourceException {
        return new JiraInteraction(this, restTemplate, authHeaders);
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
    public void close() throws ResourceException {
    }

    public String createManualReviewTask(String appName, Long appId) {
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
            ResponseEntity<String> response = restTemplate.postForEntity(
                    endpoint,
                    request,
                    String.class
            );
            logger.info("Response: {}", response.getBody());
            return extractIssueIdFromResponse(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to create Jira issue", e);
            throw new RuntimeException("Failed to create Jira issue: " + e.getMessage(), e);
        }
    }

    public void updateTaskStatus(String issueId, String status) {
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
        List<Map<String, Object>> transitions = getTransitionsForIssue(issueId);
        return findTransitionIdByStatus(transitions, status);
    }

    private List<Map<String, Object>> getTransitionsForIssue(String issueId) {
        String endpoint = jiraUrl + "/rest/api/2/issue/" + issueId + "/transitions";
        HttpEntity<String> request = new HttpEntity<>(authHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    request,
                    String.class
            );
            return parseTransitionsResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transitions for issue", e);
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
