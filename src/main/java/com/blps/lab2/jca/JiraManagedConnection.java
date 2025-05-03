package com.blps.lab2.jca;


import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ResultSetInfo;
import jakarta.resource.spi.*;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;

public class JiraManagedConnection implements ManagedConnection {

    @Override
    public Connection getConnection() throws ResourceException {
        return new JiraConnection();
    }

    @Override
    public void close() throws ResourceException {
        // Закрытие соединения с Jira
    }

    @Override
    public void start(LocalTransaction localTransaction) throws ResourceException {
        // Логика начала транзакции
    }

    @Override
    public void end(LocalTransaction localTransaction, boolean commit) throws ResourceException {
        // Логика завершения транзакции
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public void destroy() throws ResourceException {

    }

    @Override
    public void cleanup() throws ResourceException {

    }

    @Override
    public void associateConnection(Object o) throws ResourceException {

    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {

    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {

    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null; // Локальная транзакция для неуправляемого режима
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
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
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }
}
