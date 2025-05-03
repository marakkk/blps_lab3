package com.blps.lab2.jca;


import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.Set;

public class JiraManagedConnectionFactory implements ManagedConnectionFactory {

    @Override
    public JiraManagedConnection createManagedConnection(Object[] connectionInfo, XAResource xaResource) {
        return new JiraManagedConnection();
    }

    @Override
    public ConnectionFactory createConnectionFactory(ConnectionManager connectionManager) {
        return new JiraConnectionFactory(connectionManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {

    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionFactory getManagedConnectionFactory() {
        return this;
    }
}

