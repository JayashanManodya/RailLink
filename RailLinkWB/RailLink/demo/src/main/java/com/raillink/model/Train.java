package com.raillink.model;

// Database mapping and validation related imports
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import java.util.HashMap;

// @Entity – indha class oru database table ah represent pannum
@Entity
// @Table – table name “trains” nu set pannurathu
@Table(name = "trains")
public class Train {

    // @Id – primary key column nu indicate pannum
    // @GeneratedValue – id automatic generate aagum (auto increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank – name empty a iruka koodadhu
    // @Column(nullable = false) – null value allow panna koodadhu
    private String name;

    // @NotNull – null value koodadhu
    // @Positive – positive number dhan irukanum
    // Capacity total seat count ah represent pannum
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer capacity; // Total capacity (calculated from classes)

    // Train status (ACTIVE, MAINTENANCE, OUT_OF_SERVICE)
    @NotBlank
    @Column(nullable = false)
    private String status;

    // Train classes details JSON format la store pannum
    // Example: {"First Class": 50, "Second Class": 100, "Third Class": 200}
    @Column(columnDefinition = "TEXT")
    private String classesJson;

    // Default constructor (no-argument constructor)
    public Train() {}

    // Constructor – name, capacity, status vachu object create pannum
    public Train(String name, Integer capacity, String status) {
        this.name = name;
        this.capacity = capacity;
        this.status = status;
    }

    // Constructor – name, classes map, status vachu object create pannum
    // classes map -> JSON convert pannum + capacity calculate pannum
    public Train(String name, Map<String, Integer> classes, String status) {
        this.name = name;
        this.classesJson = mapToJson(classes);
        this.capacity = classes.values().stream().mapToInt(Integer::intValue).sum();
        this.status = status;
    }

    // Map<String, Integer> ah JSON string format ku convert pannum
    private String mapToJson(Map<String, Integer> classes) {
        if (classes == null || classes.isEmpty()) {
            return "{}"; // Empty map na empty JSON return pannum
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

    // JSON string ah back Map format ku convert pannum (get method)
    public Map<String, Integer> getClasses() {
        Map<String, Integer> classes = new HashMap<>();
        if (classesJson == null || classesJson.trim().isEmpty() || classesJson.equals("{}")) {
            return classes; // Empty JSON na empty map return pannum
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

    // Map format la irundha data ah JSON format ku set pannum + capacity update pannum
    public void setClasses(Map<String, Integer> classes) {
        this.classesJson = mapToJson(classes);
        this.capacity = classes.values().stream().mapToInt(Integer::intValue).sum();
    }

    // Getters and Setters (all attributes ku access methods)
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
