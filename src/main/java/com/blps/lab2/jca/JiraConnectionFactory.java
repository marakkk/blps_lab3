package com.blps.lab2.jca;


import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;

import javax.naming.NamingException;
import javax.naming.Reference;

public class JiraConnectionFactory implements ConnectionFactory {

    @Override
    public JiraConnection getConnection() {
        return new JiraConnection();
    }

    @Override
    public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
        return null;
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
    public void setReference(Reference reference) {

    }

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }
}

