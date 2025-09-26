package com.raillink.controller;

import com.raillink.model.User;
import com.raillink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            String token = userService.createPasswordResetToken(email);
            model.addAttribute("message", "If the email exists, a reset link was sent. Simulated link: /reset-password?token=" + token);
        } catch (Exception e) {
            // Do not reveal existence of email; still show generic message
            model.addAttribute("message", "If the email exists, a reset link was sent.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("token", token);
            return "reset-password";
        }
        User user = userService.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        userService.consumePasswordResetToken(user, password);
        model.addAttribute("success", "Password has been reset. You may now log in.");
        return "login";
    }
} 