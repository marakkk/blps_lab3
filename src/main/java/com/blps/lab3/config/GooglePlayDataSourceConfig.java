package com.blps.lab3.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.blps.lab3.repo.googleplay",
        entityManagerFactoryRef = "googleplayEntityManagerFactory",
        transactionManagerRef = "atomikosTransactionManager"
)
public class GooglePlayDataSourceConfig {

    @Value("${spring.datasource.googleplay.url}")
    private String dbUrl;

    @Value("${spring.datasource.googleplay.user}")
    private String dbUser;

    @Value("${spring.datasource.googleplay.password}")
    private String dbPassword;

    @Value("${googleplay.hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;

    @Value("${googleplay.hibernate.dialect}")
    private String hibernateDialect;

    @Bean(name = "googleplayEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean googleplayEntityManagerFactory(
            @Qualifier("googleplayDataSource") DataSource dataSource
    ) {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), Collections.emptyMap(), null)
                .dataSource(dataSource)
                .packages("com.blps.lab2.entities.googleplay")
                .persistenceUnit("googleplay")
                .properties(hibernateProperties())
                .jta(true)
                .build();
    }

    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.put("hibernate.dialect", hibernateDialect);
        return properties;
    }

    @Bean(name = "googleplayDataSource")
    public DataSource googleplayDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("googleplayDS");
        dataSource.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");

        Properties xaProperties = new Properties();
        xaProperties.setProperty("user", dbUser);
        xaProperties.setProperty("password", dbPassword);
        xaProperties.setProperty("url", dbUrl);

        dataSource.setXaProperties(xaProperties);
        dataSource.setMaxPoolSize(10);
        dataSource.setMinPoolSize(1);

        return dataSource;
    }

}
