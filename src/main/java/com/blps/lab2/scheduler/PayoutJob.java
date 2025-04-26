package com.blps.lab2.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayoutJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PayoutJob.class);

    @Autowired
    private PayoutService payoutService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled payout job for developers...");
        payoutService.processDeveloperPayouts();
        logger.info("Finished scheduled payout job for developers.");
    }
}

