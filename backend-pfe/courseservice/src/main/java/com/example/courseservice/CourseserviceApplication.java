package com.example.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.courseservice.client")
@SpringBootApplication
public class CourseserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseserviceApplication.class, args);
	}
}
