package br.com.fiap.clyvovet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class ClyvovetApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClyvovetApplication.class, args);
	}

}


