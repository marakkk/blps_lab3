package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class JiraConnection implements Connection, AutoCloseable {

    private final RestTemplate restTemplate;
    private final HttpHeaders adminHeaders;
    private final HttpHeaders moderatorHeaders;
    private final String jiraUrl;
    private final String jiraProjectKey;
    private final String assignee;

    @Override
    public Interaction createInteraction() throws ResourceException {
        return new JiraInteraction(this, restTemplate, adminHeaders, moderatorHeaders, jiraUrl, jiraProjectKey, assignee);
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
