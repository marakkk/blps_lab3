package com.blps.lab2.config;

import com.blps.lab2.scheduler.PayoutJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

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
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("paymentsDataSource") DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setDataSource(dataSource);

        return factory;
    }
}
