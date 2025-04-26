package com.blps.lab2.config;

import com.blps.lab2.scheduler.PayoutJob;
import lombok.RequiredArgsConstructor;
import org.postgresql.ds.PGSimpleDataSource;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.autoconfigure.quartz.QuartzTransactionManager;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {
    @Value("${spring.datasource.payments.url}")
    private String dbUrl;

    @Value("${spring.datasource.payments.user}")
    private String dbUser;

    @Value("${spring.datasource.payments.password}")
    private String dbPassword;

    private final PayoutJobProperties payoutJobProperties;

    @Bean
    public JobDetail payoutJobDetail() {
        return JobBuilder.newJob(PayoutJob.class)
                .withIdentity("payoutJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger payoutJobTrigger(JobDetail payoutJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(payoutJobDetail)
                .withIdentity("payoutTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(payoutJobProperties.getCron()))
                .build();
    }

    @Bean
    @QuartzTransactionManager
    public PlatformTransactionManager quartzTransactionManager(
            @QuartzDataSource DataSource dataSource
    ) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource() {
        return DataSourceBuilder
                .create()
                .username(dbUser)
                .password(dbPassword)
                .url(dbUrl)
                .driverClassName("org.postgresql.Driver")
                .type(PGSimpleDataSource.class)
                .build();
    }

    @Bean
    public QuartzDataSourceScriptDatabaseInitializer quartzDataSourceScriptDatabaseInitializer(
            @QuartzDataSource DataSource quartzDataSource,
            QuartzProperties properties) {
        return new QuartzDataSourceScriptDatabaseInitializer(quartzDataSource, properties);
    }
}
