package org.example.moskacalculatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoskaCalculatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoskaCalculatorServiceApplication.class, args);
    }

}
