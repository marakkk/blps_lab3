package com.blps.lab2;

import com.blps.lab2.config.PayoutJobProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableMethodSecurity
@EnableConfigurationProperties(PayoutJobProperties.class)
public class GooglePlayApp {
	public static void main(String[] args) {
		SpringApplication.run(GooglePlayApp.class, args);
	}
}

