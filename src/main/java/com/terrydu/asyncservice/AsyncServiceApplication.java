package com.terrydu.asyncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
		"com.terrydu.asyncservice",
		"com.terrydu.asyncservice.api.jersey.controller"})
public class AsyncServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncServiceApplication.class, args);
	}

}
