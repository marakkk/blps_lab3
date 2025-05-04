package com.blps.lab3.resourceAdapter;


import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;

import javax.naming.NamingException;
import javax.naming.Reference;

public class JiraConnectionFactory implements ConnectionFactory {
    @Override
    public JiraConnection getConnection() {
        return new JiraConnection();
    }

    @Override
    public JiraConnection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
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
}
