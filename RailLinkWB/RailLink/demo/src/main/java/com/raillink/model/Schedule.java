package com.raillink.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "departureDate", nullable = false)
    private LocalDateTime departureDate;
    @NotNull
    @Column(name = "arrivalDate", nullable = false)
    private LocalDateTime arrivalDate;
    @Column(nullable = false)
    private String status; // ON_TIME, DELAYED, CANCELLED
    @Column
    private String scheduleName; // Custom name for the schedule
    @Column
    private Integer delayMinutes; // Minutes of delay if status is DELAYED
    @Column(columnDefinition = "TEXT")
    private String pricingJson; // JSON format: {"First Class": 1500.00, "Second Class": 1000.00, "Third Class": 500.00}
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    public Schedule() {}
    public Schedule(LocalDateTime departureDate, LocalDateTime arrivalDate, String status, Train train, Route route) {
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.status = status;
        this.train = train;
        this.route = route;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDepartureDate() {
        return departureDate;
    }
    public void setDepartureDate(LocalDateTime departureDate) {
        this.departureDate = departureDate;
    }
    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }
    public void setArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Train getTrain() {
        return train;
    }
    public void setTrain(Train train) {
        this.train = train;
    }
    public Route getRoute() {
        return route;
    }
    public void setRoute(Route route) {
        this.route = route;
    }
    public String getScheduleName() {
        return scheduleName;
    }
    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }
    public Integer getDelayMinutes() {
        return delayMinutes;
    }
    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }
    public String getPricingJson() {
        return pricingJson;
    }
    public void setPricingJson(String pricingJson) {
        this.pricingJson = pricingJson;
    }
    public void setPricing(java.util.Map<String, java.math.BigDecimal> pricing) {
        if (pricing == null || pricing.isEmpty()) {
            this.pricingJson = "{}";
            return;
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, java.math.BigDecimal> entry : pricing.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("}");
        this.pricingJson = json.toString();
    }
    public java.util.Map<String, java.math.BigDecimal> getPricing() {
        java.util.Map<String, java.math.BigDecimal> pricing = new java.util.HashMap<>();
        if (pricingJson == null || pricingJson.trim().isEmpty() || pricingJson.equals("{}")) {
            return pricing;
        }
        try {
            String json = pricingJson.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                if (!json.isEmpty()) {
                    String[] pairs = json.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            java.math.BigDecimal value = new java.math.BigDecimal(keyValue[1].trim());
                            pricing.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing pricing JSON: " + e.getMessage());
        }
        return pricing;
    }
    public String getDuration() {
        if (departureDate == null || arrivalDate == null) {
            return "N/A";
        }
        long minutes = java.time.Duration.between(departureDate, arrivalDate).toMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, remainingMinutes);
        } else {
            return String.format("%dm", remainingMinutes);
        }
    }
} 