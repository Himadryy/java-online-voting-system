package com.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VotingWebApplication {

	public static void main(String[] args) {
		// Tell Spring Boot to ignore macOS hidden metadata files (._*)
		System.setProperty("spring.classformat.ignore", "true");
		SpringApplication.run(VotingWebApplication.class, args);
	}

}
