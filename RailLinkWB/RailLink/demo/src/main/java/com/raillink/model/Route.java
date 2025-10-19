package com.raillink.model; // package name (unga project la model package ku belong aaguthu)

import jakarta.persistence.Column; // database column define panna use aagura annotation
import jakarta.persistence.Entity; // entity nu indicate panna (table represent panna)
import jakarta.persistence.GeneratedValue; // auto id generate panna use aagum
import jakarta.persistence.GenerationType; // id generate type define panna
import jakarta.persistence.Id; // primary key nu mark panna
import jakarta.persistence.Table; // table name specify panna
import jakarta.validation.constraints.NotBlank; // empty a iruka kudathu nu check panna

// ---- Database table oda model class ----
@Entity // indha class database table ah represent panra
@Table(name = "routes") // table name 'routes' nu specify pannirukom
public class Route {

    @Id // primary key column nu solludhu
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id auto-increment aagum (DB la generate aagum)
    private Long id; // unique ID for each route record

    @NotBlank // empty name kuduka kudathu
    @Column(unique = true, nullable = false) // unique ah irukanum, null allow panna koodadhu
    private String name; // route name (example: Chennai Express)

    @Column // description optional field (null allow pannirukom)
    private String description; // route path description or details

    @NotBlank // empty code kuduka kudathu
    @Column(unique = true, nullable = false, length = 50) // unique code, max length 50
    private String routeCode; // example: CN-001, KD-002 — route ku special code

    @NotBlank // path empty a iruka kudathu
    @Column(nullable = false, length = 2000) // null allow panna koodadhu, max 2000 chars
    private String path; // station IDs comma separated (e.g., "1,2,3,4,5")

    // ---- Default constructor ----
    public Route() {}

    // ---- Parameterized constructor ----
    public Route(String name, String description, String routeCode, String path) {
        this.name = name; // name assign panna
        this.description = description; // description assign panna
        this.routeCode = routeCode; // route code assign panna
        this.path = path; // path assign panna
    }

    // ---- Getters and Setters (data access & update methods) ----

    public Long getId() { // id get panna
        return id;
    }

    public void setId(Long id) { // id set panna
        this.id = id;
    }

    public String getName() { // name get panna
        return name;
    }

    public void setName(String name) { // name set panna
        this.name = name;
    }

    public String getDescription() { // description get panna
        return description;
    }

    public void setDescription(String description) { // description set panna
        this.description = description;
    }

    public String getRouteCode() { // route code get panna
        return routeCode;
    }

    public void setRouteCode(String routeCode) { // route code set panna
        this.routeCode = routeCode;
    }

    public String getPath() { // path get panna
        return path;
    }

    public void setPath(String path) { // path set panna
        this.path = path;
    }
}
