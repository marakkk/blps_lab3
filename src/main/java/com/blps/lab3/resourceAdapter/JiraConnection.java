package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class JiraConnection implements Connection, AutoCloseable {

    private final RestTemplate restTemplate;
    private final HttpHeaders authHeaders;
    private final String jiraUrl;
    private final String jiraProjectKey;
    private final String defaultAssignee;

    @Override
    public Interaction createInteraction() throws ResourceException {
        return new JiraInteraction(this, restTemplate, authHeaders, jiraUrl, jiraProjectKey, defaultAssignee);
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

}
