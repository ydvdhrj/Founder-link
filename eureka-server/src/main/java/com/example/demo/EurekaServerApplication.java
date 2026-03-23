package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
	
	// Standard logger setup - no lombok required
	private static final Logger log = LoggerFactory.getLogger(EurekaServerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
		log.info("FounderLink Eureka Discovery Server started succefully!");
	}

}
