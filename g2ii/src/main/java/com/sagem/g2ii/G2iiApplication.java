package com.sagem.g2ii;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.sagem.g2ii.Entity")
@EnableScheduling // <--- C'EST ICI QU'IL FAUT L'AJOUTER !
public class G2iiApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2iiApplication.class, args);
	}

}
