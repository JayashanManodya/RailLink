package com.raillink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RailLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(RailLinkApplication.class, args);
	}

} 