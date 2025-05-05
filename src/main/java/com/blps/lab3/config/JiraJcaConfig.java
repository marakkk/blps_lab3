package com.blps.lab3.config;

import com.blps.lab3.resourceAdapter.JiraConnectionFactory;
import com.blps.lab3.resourceAdapter.JiraManagedConnectionFactory;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiraJcaConfig {

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.username}")
    private String jiraUsername;

    @Value("${jira.token}")
    private String jiraApiToken;

    @Value("${jira.projectKey}")
    private String jiraProjectKey;

    @Value("${jira.assignee}")
    private String defaultAssignee;

    @Bean
    public ConnectionFactory jiraManagedConnectionFactory() throws ResourceException {
        JiraManagedConnectionFactory mcf = new JiraManagedConnectionFactory();
        mcf.setJiraUrl(jiraUrl);
        mcf.setJiraUsername(jiraUsername);
        mcf.setJiraApiToken(jiraApiToken);
        mcf.setJiraProjectKey(jiraProjectKey);
        mcf.setDefaultAssignee(defaultAssignee);
        return (JiraConnectionFactory) mcf.createConnectionFactory();
    }

}

