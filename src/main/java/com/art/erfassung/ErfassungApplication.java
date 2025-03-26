package com.art.erfassung;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ErfassungApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErfassungApplication.class, args);
	}
}
