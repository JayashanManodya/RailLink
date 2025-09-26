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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    // Default constructor
    public Schedule() {}
    
    // Constructor with all fields
    public Schedule(LocalDateTime departureDate, LocalDateTime arrivalDate, String status, Train train, Route route) {
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.status = status;
        this.train = train;
        this.route = route;
    }
    
    // Getters and Setters
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
} 