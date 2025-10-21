package com.raillink.controller;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
@RestController
public class DbHealthController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSource dataSource;
    @GetMapping("/api/db-health")
    public Map<String, Object> dbHealth() {
        Map<String, Object> resp = new HashMap<>();
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            resp.put("database", one != null && one == 1 ? "UP" : "UNKNOWN");
        } catch (RuntimeException e) {
            resp.put("database", "DOWN");
            resp.put("error", e.getMessage());
        }
        return resp;
    }
    @GetMapping("/api/db-info")
    public Map<String, Object> dbInfo() {
        Map<String, Object> resp = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = conn.getMetaData().getURL();
            String currentDb = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
            resp.put("jdbcUrl", jdbcUrl);
            resp.put("database", currentDb);
            resp.put("users", userCount);
            resp.put("status", "OK");
        } catch (Exception e) {
            resp.put("status", "ERROR");
            resp.put("error", e.getMessage());
        }
        return resp;
    }
}
