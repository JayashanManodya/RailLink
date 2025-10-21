package com.raillink.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
@Entity
@Table(name = "schedule_stations")
public class ScheduleStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;
    @NotNull
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;
    @NotNull
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;
    @Column(name = "station_order", nullable = false)
    private Integer stationOrder; // Order of station in the route
    @Column
    private String platform; // Platform number at the station
    @Column
    private Integer delayMinutes; // Delay at this specific station
    public ScheduleStation() {}
    public ScheduleStation(Schedule schedule, Station station, LocalDateTime arrivalTime, 
                          LocalDateTime departureTime, Integer stationOrder) {
        this.schedule = schedule;
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stationOrder = stationOrder;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Schedule getSchedule() {
        return schedule;
    }
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    public Station getStation() {
        return station;
    }
    public void setStation(Station station) {
        this.station = station;
    }
    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    public Integer getStationOrder() {
        return stationOrder;
    }
    public void setStationOrder(Integer stationOrder) {
        this.stationOrder = stationOrder;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public Integer getDelayMinutes() {
        return delayMinutes;
    }
    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }
}
