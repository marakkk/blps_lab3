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
    private String username;

    @Value("${jira.token}")
    private String token;

    @Value("${jira.projectKey}")
    private String jiraProjectKey;

    @Value("${jira.assignee}")
    private String assignee;

    @Value("${jira.assignee_pass}")
    private String assigneePass;

    @Bean
    public ConnectionFactory jiraManagedConnectionFactory() throws ResourceException {
        JiraManagedConnectionFactory mcf = new JiraManagedConnectionFactory();
        mcf.setJiraUrl(jiraUrl);
        mcf.setUsername(username);
        mcf.setToken(token);
        mcf.setJiraProjectKey(jiraProjectKey);
        mcf.setAssignee(assignee);
        mcf.setAssigneePass(assigneePass);
        return (JiraConnectionFactory) mcf.createConnectionFactory();
    }

}

