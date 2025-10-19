package com.raillink.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/cssticket").hasRole("PASSENGER")
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/api/test/**", "/api/health").permitAll()
                .requestMatchers("/api/sse/**").permitAll()
                .requestMatchers("/api/admin/routes").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/staff/**").hasAnyRole("STAFF","ADMIN")
                .requestMatchers("/api/bookings/**").hasRole("PASSENGER")
                .requestMatchers("/api/profile/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        return (request, response, authentication) -> {
            String targetUrl = "/search-trains";
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    targetUrl = "/admin/dashboard";
                    break;
                } else if ("ROLE_STAFF".equals(role)) {
                    targetUrl = "/staff/dashboard";
                    break;
                }
            }
            redirectStrategy.sendRedirect(request, response, targetUrl);
        };
    }
} 