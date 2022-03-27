package com.alderaeney.mangaloidebackend;

import com.alderaeney.mangaloidebackend.security.SecurityConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SecurityConfiguration.class })
public class MangaloideBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MangaloideBackendApplication.class, args);
	}

}
