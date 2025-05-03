package com.blps.lab2.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkManager;
import org.apache.log4j.Logger;

import javax.transaction.xa.XAResource;

public class JiraResourceAdapter implements ResourceAdapter {

    private static final Logger logger = Logger.getLogger(JiraResourceAdapter.class.getName());
    private BootstrapContext bootstrapContext;
    private JiraConnectionManager connectionManager;

    @Override
    public void start(BootstrapContext ctx) {
        this.bootstrapContext = ctx;
        try {
            connectionManager = new JiraConnectionManager();
            connectionManager.connect();
            logger.info("Jira Resource Adapter started and connected to Jira.");
        } catch (Exception e) {
            logger.error("Failed to start Jira Resource Adapter: ", e);
            throw new RuntimeException("Failed to start Jira Resource Adapter", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (connectionManager != null) {
                connectionManager.disconnect();
                logger.info("Jira Resource Adapter stopped and disconnected from Jira.");
            }
        } catch (Exception e) {
            logger.error("Failed to stop Jira Resource Adapter: ", e);
        }
    }

    @Override
    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return null;
    }

    @Override
    public WorkManager getWorkManager() {
        return bootstrapContext != null ? bootstrapContext.getWorkManager() : null;
    }

    @Override
    public XATerminator getXATerminator() {
        return bootstrapContext != null ? bootstrapContext.getXATerminator() : null;
    }
}
