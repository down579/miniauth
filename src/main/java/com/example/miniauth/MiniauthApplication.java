package com.example.miniauth;

import com.example.miniauth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MiniauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniauthApplication.class, args);
    }

}
