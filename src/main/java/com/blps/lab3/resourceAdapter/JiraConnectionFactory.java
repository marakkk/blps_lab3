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
        HttpHeaders adminHeaders = createAuthHeaders(mcf.getUsername(), mcf.getToken());
        HttpHeaders moderatorHeaders = createAuthHeaders(mcf.getAssignee(), mcf.getAssigneePass());

        return new JiraConnection(
                restTemplate,
                adminHeaders,
                moderatorHeaders,
                mcf.getJiraUrl(),
                mcf.getJiraProjectKey(),
                mcf.getAssignee()
        );
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

    private HttpHeaders createAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = username + ":" + password;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        return headers;
    }
}
