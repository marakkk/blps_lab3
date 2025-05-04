package com.blps.lab3.config;

import com.blps.lab3.resourceAdapter.JiraManagedConnectionFactory;
import com.blps.lab3.resourceAdapter.JiraResourceAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jca.support.LocalConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jca.support.ResourceAdapterFactoryBean;
import org.springframework.jca.support.SimpleBootstrapContext;

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
    public JiraManagedConnectionFactory jiraManagedConnectionFactory() {
        JiraManagedConnectionFactory mcf = new JiraManagedConnectionFactory();
        mcf.setJiraUrl(jiraUrl);
        mcf.setUsername(jiraUsername);
        mcf.setApiToken(jiraApiToken);
        mcf.setJiraProjectKey(jiraProjectKey);
        mcf.setDefaultAssignee(defaultAssignee);
        return mcf;
    }

    @Bean
    public LocalConnectionFactoryBean jiraConnectionFactory(JiraManagedConnectionFactory mcf) {
        LocalConnectionFactoryBean factoryBean = new LocalConnectionFactoryBean();
        factoryBean.setManagedConnectionFactory(mcf);
        return factoryBean;
    }

    @Bean
    public SimpleBootstrapContext simpleBootstrapContext() {
        return new SimpleBootstrapContext(null);
    }

    @Bean
    public ResourceAdapterFactoryBean jiraResourceAdapter(SimpleBootstrapContext bootstrapContext) {
        ResourceAdapterFactoryBean bean = new ResourceAdapterFactoryBean();
        bean.setResourceAdapter(new JiraResourceAdapter());
        bean.setBootstrapContext(bootstrapContext);
        return bean;
    }

}

