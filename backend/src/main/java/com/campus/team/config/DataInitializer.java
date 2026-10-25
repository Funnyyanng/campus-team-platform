package com.campus.team.config;

import com.campus.team.entity.User;
import com.campus.team.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin-email:}")
    private String adminEmail;

    @Value("${bootstrap.admin-password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException("Set both administrator bootstrap environment variables.");
        }
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNickname("系统管理员");
            admin.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=admin");
            admin.setRole("ADMIN");
            admin.setStatus("NORMAL");
            admin.setAllowInvite(false);
            admin.setCollege("系统");
            admin.setMajor("管理");
            admin.setGrade("管理员");
            userRepository.save(admin);
            log.info("Configured administrator account initialized.");
        }
    }
}
