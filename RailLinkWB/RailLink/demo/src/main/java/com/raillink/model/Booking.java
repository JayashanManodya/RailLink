package com.raillink.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(nullable = false)
    private LocalDateTime bookingDate;
    @NotBlank
    @Column(nullable = false)
    private String seatNumber;
    @Column(nullable = false)
    private String status;
    @Column(name = "fare", precision = 10, scale = 2)
    private BigDecimal fare;
    
    @Column(name = "ticket_class")
    private String ticketClass;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;
    public Booking() {}
    public Booking(LocalDateTime bookingDate, String seatNumber, String status, User user, Schedule schedule) {
        this.bookingDate = bookingDate;
        this.seatNumber = seatNumber;
        this.status = status;
        this.user = user;
        this.schedule = schedule;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
    public String getSeatNumber() {
        return seatNumber;
    }
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public BigDecimal getFare() {
        return fare;
    }
    public void setFare(BigDecimal fare) {
        this.fare = fare;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Schedule getSchedule() {
        return schedule;
    }
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    
    public String getTicketClass() {
        return ticketClass;
    }
    
    public void setTicketClass(String ticketClass) {
        this.ticketClass = ticketClass;
    }
} 