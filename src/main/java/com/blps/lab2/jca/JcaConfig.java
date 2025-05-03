package com.blps.lab2.jca;

import org.springframework.jca.cci.connection.CciLocalTransactionManager;
import org.springframework.jca.support.LocalConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jca.support.ResourceAdapterFactoryBean;
import org.springframework.jca.support.SimpleBootstrapContext;

@Configuration
public class JcaConfig {
//    @Bean
//    public SimpleBootstrapContext bootstrapContext() {
//        return new SimpleBootstrapContext();
//    }

    @Bean
    public ResourceAdapterFactoryBean resourceAdapter(SimpleBootstrapContext context) {
        ResourceAdapterFactoryBean factory = new ResourceAdapterFactoryBean();
        factory.setBootstrapContext(context);
        factory.setResourceAdapter(new JiraResourceAdapter());
        return factory;
    }

    @Bean
    public JiraManagedConnectionFactory jiraManagedConnectionFactory(ResourceAdapterFactoryBean resourceAdapter) {
        JiraManagedConnectionFactory factory = new JiraManagedConnectionFactory();
        //factory.setResourceAdapter(resourceAdapter.getObject());
        return factory;
    }

    @Bean
    public LocalConnectionFactoryBean jiraConnectionFactory(JiraManagedConnectionFactory jiraManagedConnectionFactory) {
        LocalConnectionFactoryBean factoryBean = new LocalConnectionFactoryBean();
        factoryBean.setManagedConnectionFactory(jiraManagedConnectionFactory);
        return factoryBean;
    }

    @Bean
    public CciLocalTransactionManager transactionManager() {
        return new CciLocalTransactionManager();
    }
}


