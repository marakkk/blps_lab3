package com.blps.lab3.resourceAdapter;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JiraManagedConnectionFactory implements ManagedConnectionFactory {

    private String jiraUrl;
    private String username;
    private String token;
    private String jiraProjectKey;
    private String assignee;
    private String assigneePass;

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        throw new ResourceException("ConnectionManager not supported");
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new JiraConnectionFactory(this);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        throw new ResourceException("Managed connections not supported");
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connections, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        throw new ResourceException("Managed connections not supported");
    }

    @Override
    public void setLogWriter(PrintWriter writer) {}

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
}


