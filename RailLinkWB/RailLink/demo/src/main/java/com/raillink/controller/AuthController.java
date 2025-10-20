package com.raillink.controller;
import com.raillink.model.Role;
import com.raillink.model.User;
import com.raillink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@Controller
public class AuthController {
    @Autowired
    private UserService userService;
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String roleStr = request.get("role");
            Role role = Role.ROLE_PASSENGER; // Default role
            if ("ADMIN".equalsIgnoreCase(roleStr)) {
                role = Role.ROLE_ADMIN;
            } else if ("STAFF".equalsIgnoreCase(roleStr)) {
                role = Role.ROLE_STAFF;
            }
            User user = userService.registerUser(username, email, password, role);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 