package com.joel.br.FlightMatrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableCaching
@EnableScheduling
public class FlightMatrixApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightMatrixApplication.class, args);
	}

}
