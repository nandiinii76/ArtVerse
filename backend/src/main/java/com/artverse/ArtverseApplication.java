package com.artverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArtverseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtverseApplication.class, args);
    }
}
