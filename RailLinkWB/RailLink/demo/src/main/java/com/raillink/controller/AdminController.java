package com.raillink.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raillink.model.Refund;
import com.raillink.model.Route;
import com.raillink.model.Schedule;
import com.raillink.model.Station;
import com.raillink.model.Train;
import com.raillink.service.RefundService;
import com.raillink.service.RouteService;
import com.raillink.service.ScheduleService;
import com.raillink.service.ScheduleUpdateBroadcaster;
import com.raillink.service.StationService;
import com.raillink.service.TrainService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TrainService trainService;

    @Autowired
    private StationService stationService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private ScheduleUpdateBroadcaster scheduleBroadcaster;

    // Train endpoints
    @GetMapping("/trains")
    public ResponseEntity<List<Train>> getAllTrains() {
        return ResponseEntity.ok(trainService.findAllTrains());
    }

    @PostMapping("/trains")
    public ResponseEntity<Train> createTrain(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Integer capacity = (Integer) request.get("capacity");
        String status = (String) request.get("status");
        
        Train train = trainService.createTrain(name, capacity, status);
        return ResponseEntity.ok(train);
    }

    @PutMapping("/trains/{id}")
    public ResponseEntity<Train> updateTrain(@PathVariable Long id, @RequestBody Train train) {
        Train updatedTrain = trainService.updateTrain(id, train);
        return ResponseEntity.ok(updatedTrain);
    }

    @DeleteMapping("/trains/{id}")
    public ResponseEntity<?> deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return ResponseEntity.ok().build();
    }

    // Station endpoints
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationService.findAllStations());
    }

    @PostMapping("/stations")
    public ResponseEntity<Station> createStation(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String location = request.get("location");
        
        Station station = stationService.createStation(name, location);
        return ResponseEntity.ok(station);
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @RequestBody Station station) {
        Station updatedStation = stationService.updateStation(id, station);
        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.ok().build();
    }

    // Route endpoints
    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.findAllRoutes());
    }

    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        
        Route route = routeService.createRoute(name, description);
        return ResponseEntity.ok(route);
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route route) {
        Route updatedRoute = routeService.updateRoute(id, route);
        return ResponseEntity.ok(updatedRoute);
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok().build();
    }

    // Schedule endpoints
    @GetMapping("/schedules")
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    @PostMapping("/schedules")
    public ResponseEntity<Schedule> createSchedule(@RequestBody Map<String, Object> request) {
        LocalDateTime departureDate = LocalDateTime.parse((String) request.get("departureDate"));
        LocalDateTime arrivalDate = LocalDateTime.parse((String) request.get("arrivalDate"));
        String status = (String) request.get("status");
        Long trainId = Long.valueOf((Integer) request.get("trainId"));
        Long routeId = Long.valueOf((Integer) request.get("routeId"));
        
        Train train = trainService.findTrainById(trainId).orElseThrow();
        Route route = routeService.findRouteById(routeId).orElseThrow();
        
        Schedule schedule = scheduleService.createSchedule(departureDate, arrivalDate, status, train, route);
        scheduleBroadcaster.broadcastScheduleCreated(schedule);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updatedSchedule = scheduleService.updateSchedule(id, schedule);
        scheduleBroadcaster.broadcastScheduleUpdate(updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        scheduleBroadcaster.broadcastScheduleDeleted(id);
        return ResponseEntity.ok().build();
    }

    // Refund endpoints
    @PostMapping("/refunds/request")
    public ResponseEntity<Refund> requestRefund(@RequestBody Map<String, Object> request) {
        Long bookingId = Long.valueOf(request.get("bookingId").toString());
        java.math.BigDecimal amount = new java.math.BigDecimal(request.get("amount").toString());
        String reason = request.getOrDefault("reason", "").toString();
        Refund refund = refundService.requestRefund(bookingId, amount, reason);
        return ResponseEntity.ok(refund);
    }

    @PostMapping("/refunds/{id}/approve")
    public ResponseEntity<Refund> approveRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.approveRefund(id, admin));
    }

    @PostMapping("/refunds/{id}/reject")
    public ResponseEntity<Refund> rejectRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.rejectRefund(id, admin));
    }

    @PostMapping("/refunds/{id}/issued")
    public ResponseEntity<Refund> markRefundIssued(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.markIssued(id, admin));
    }
} 