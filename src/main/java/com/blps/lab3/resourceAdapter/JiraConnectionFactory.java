package com.blps.lab3.resourceAdapter;


import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Base64;

@RequiredArgsConstructor
public class JiraConnectionFactory implements ConnectionFactory {
    private final JiraManagedConnectionFactory mcf;

    @Override
    public JiraConnection getConnection() {
        RestTemplate restTemplate = createRestTemplate();
        HttpHeaders authHeaders = createAuthHeaders();
        return new JiraConnection(
                restTemplate,
                authHeaders,
                mcf.getJiraUrl(),
                mcf.getJiraProjectKey(),
                mcf.getDefaultAssignee());
    }

    @Override
    public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
        return getConnection();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public void setReference(Reference reference) {}

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }

    private RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = mcf.getJiraUsername() + ":" + mcf.getJiraApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
