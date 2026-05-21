package com.re.busticketpro.config;

import com.re.busticketpro.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoPasswordRepairRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoPasswordRepairRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        replaceDemoHash("admin", "admin123");
        replaceDemoHash("staff01", "staff123");
        replaceDemoHash("passenger01", "passenger123");
    }

    private void replaceDemoHash(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getPasswordHash() != null && user.getPasswordHash().contains("demo_")) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            }
        });
    }
}
