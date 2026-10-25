package com.campus.team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusTeamApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusTeamApplication.class, args);
    }
}
