package com.raillink.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import com.raillink.model.Role;
@Service
public class DataInitializationService implements CommandLineRunner {
    @Autowired
    private UserService userService;
    @Override
    public void run(String... args) {
        createAdminUser();
    }
    private void createAdminUser() {
        try {
            if (userService.findByUsername("admin").isEmpty()) {
                userService.registerUser("admin", "admin@raillink.com", "admin123", Role.ROLE_ADMIN);
                System.out.println("Admin user created successfully!");
                System.out.println("Login with - Username: admin, Password: admin123");
            } else {
                System.out.println("Admin user already exists");
            }
        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}