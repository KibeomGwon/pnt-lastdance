package com.pnt.pnt_spring;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableMongoAuditing
@SpringBootApplication
public class PntSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PntSpringApplication.class, args);
	}

	@Bean
	public DateTimeProvider auditingDateTimeProvider() {
		// 현재 시간을 OffsetDateTime으로 반환
		return () -> Optional.of(OffsetDateTime.now());
	}
}
