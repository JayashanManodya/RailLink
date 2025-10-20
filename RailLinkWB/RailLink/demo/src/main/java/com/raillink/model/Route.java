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

    @Id // primary key column nu solludhu
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @NotBlank // empty name kuduka kudathu
    @Column(unique = true, nullable = false) 
    private String name; 

    @Column // description optional field (null allow pannirukom)
    private String description; 

    @NotBlank // empty code kuduka kudathu
    @Column(unique = true, nullable = false, length = 50) 
    private String routeCode; 

    @NotBlank // path empty a iruka kudathu
    @Column(nullable = false, length = 2000) 
    private String path; 

    
    public Route() {}

    // Parameterized constructor ----
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
