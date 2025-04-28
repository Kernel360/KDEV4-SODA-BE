package com.soda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
@EnableMongoRepositories
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class SodaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SodaApplication.class, args);
	}

}
