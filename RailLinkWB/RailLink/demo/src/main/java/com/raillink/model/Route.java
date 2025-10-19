package com.raillink.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;
    @Column
    private String description;
    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String routeCode; // Route code like CN-001, KD-002, etc.
    @NotBlank
    @Column(nullable = false, length = 2000)
    private String path; // Station IDs in ascending order separated by commas (e.g., "1,2,3,4,5")
    public Route() {}
    public Route(String name, String description, String routeCode, String path) {
        this.name = name;
        this.description = description;
        this.routeCode = routeCode;
        this.path = path;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getRouteCode() {
        return routeCode;
    }
    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
} 