package com.terrydu.asyncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan({
		"com.terrydu.asyncservice",
		"com.terrydu.asyncservice.api.mvc.controller",
		"com.terrydu.asyncservice.api.jersey.controller"})
public class AsyncServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncServiceApplication.class, args);
	}

}
