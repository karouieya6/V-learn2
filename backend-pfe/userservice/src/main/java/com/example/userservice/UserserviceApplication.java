package com.example.userservice;

import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@EnableDiscoveryClient

@SpringBootApplication
public class UserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}
	@Configuration
	public class RestTemplateConfig {

		@Bean
		@LoadBalanced
		public RestTemplate restTemplate() {
			RestTemplate restTemplate = new RestTemplate();

			restTemplate.getInterceptors().add((request, body, execution) -> {
				var context = SecurityContextHolder.getContext();
				if (context != null && context.getAuthentication() != null) {
					Object credentials = context.getAuthentication().getCredentials();
					if (credentials instanceof String token) {
						request.getHeaders().setBearerAuth(token);
					}
				}
				return execution.execute(request, body);
			});

			return restTemplate;
		}
	}


	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			String adminEmail = "admin@example.com";
			if (!userRepository.existsByEmail(adminEmail)) {
				AppUser admin = new AppUser();
				admin.setUsername("admin");
				admin.setEmail(adminEmail);
				admin.setPassword(encoder.encode("admin123")); // ✅ Default password
				admin.setRoles(Set.of("ADMIN"));
				admin.setActive(true);
				admin.setFirstName("System");
				admin.setLastName("Administrator");
				admin.setPhone("+21612345678");
				userRepository.save(admin);
				System.out.println("✅ Default ADMIN user created");
			}
		};
	}

}
