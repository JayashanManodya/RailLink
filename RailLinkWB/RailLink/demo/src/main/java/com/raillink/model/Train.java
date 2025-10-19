package com.raillink.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import java.util.HashMap;
@Entity
@Table(name = "trains")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable = false)
    private String name;
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer capacity; // Total capacity (calculated from classes)
    @NotBlank
    @Column(nullable = false)
    private String status; // ACTIVE, MAINTENANCE, OUT_OF_SERVICE
    @Column(columnDefinition = "TEXT")
    private String classesJson; // JSON format: {"First Class": 50, "Second Class": 100, "Third Class": 200}
    public Train() {}
    public Train(String name, Integer capacity, String status) {
        this.name = name;
        this.capacity = capacity;
        this.status = status;
    }
    public Train(String name, Map<String, Integer> classes, String status) {
        this.name = name;
        this.classesJson = mapToJson(classes);
        this.capacity = classes.values().stream().mapToInt(Integer::intValue).sum();
        this.status = status;
    }
    private String mapToJson(Map<String, Integer> classes) {
        if (classes == null || classes.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : classes.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    public Map<String, Integer> getClasses() {
        Map<String, Integer> classes = new HashMap<>();
        if (classesJson == null || classesJson.trim().isEmpty() || classesJson.equals("{}")) {
            return classes;
        }
        try {
            String json = classesJson.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                if (!json.isEmpty()) {
                    String[] pairs = json.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            Integer value = Integer.parseInt(keyValue[1].trim());
                            classes.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing classes JSON: " + e.getMessage());
        }
        return classes;
    }
    public void setClasses(Map<String, Integer> classes) {
        this.classesJson = mapToJson(classes);
        this.capacity = classes.values().stream().mapToInt(Integer::intValue).sum();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getClassesJson() {
        return classesJson;
    }
    public void setClassesJson(String classesJson) {
        this.classesJson = classesJson;
    }
} 