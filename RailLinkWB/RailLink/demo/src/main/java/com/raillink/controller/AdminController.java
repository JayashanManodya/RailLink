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
    @GetMapping("/trains")
    public ResponseEntity<List<Train>> getAllTrains() {
        return ResponseEntity.ok(trainService.findAllTrains());
    }
    @GetMapping("/trains/{id}")
    public ResponseEntity<Train> getTrain(@PathVariable Long id) {
        Train train = trainService.findTrainById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));
        return ResponseEntity.ok(train);
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
    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.findAllRoutes());
    }
    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String routeCode = request.get("routeCode");
        String path = request.get("path");
        Route route = routeService.createRoute(name, description, routeCode, path);
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
    @GetMapping("/routes/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable Long id) {
        Route route = routeService.findRouteById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        return ResponseEntity.ok(route);
    }
    // Get all schedules
    @GetMapping("/schedules")
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        // Fetch all schedules from scheduleService and return with HTTP 200 OK
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    // Create a new schedule
    @PostMapping("/schedules")
    public ResponseEntity<Schedule> createSchedule(@RequestBody Map<String, Object> request) {
        // Parse departure date from request body
        LocalDateTime departureDate = LocalDateTime.parse((String) request.get("departureDate"));

        // Parse arrival date from request body
        LocalDateTime arrivalDate = LocalDateTime.parse((String) request.get("arrivalDate"));

        // Get status value from request body
        String status = (String) request.get("status");

        // Get trainId from request body and convert to Long
        Long trainId = Long.valueOf((Integer) request.get("trainId"));

        // Get routeId from request body and convert to Long
        Long routeId = Long.valueOf((Integer) request.get("routeId"));

        // Fetch Train object by ID or throw exception if not found
        Train train = trainService.findTrainById(trainId).orElseThrow();

        // Fetch Route object by ID or throw exception if not found
        Route route = routeService.findRouteById(routeId).orElseThrow();

        // Create new schedule using the scheduleService
        Schedule schedule = scheduleService.createSchedule(departureDate, arrivalDate, status, train, route);

        // Broadcast the newly created schedule to subscribers (real-time update)
        scheduleBroadcaster.broadcastScheduleCreated(schedule);

        // Return the created schedule with HTTP 200 OK
        return ResponseEntity.ok(schedule);
    }

    // Update an existing schedule by ID
    @PutMapping("/schedules/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        // Update the schedule using scheduleService
        Schedule updatedSchedule = scheduleService.updateSchedule(id, schedule);

        // Broadcast the updated schedule to subscribers (real-time update)
        scheduleBroadcaster.broadcastScheduleUpdate(updatedSchedule);

        // Return the updated schedule with HTTP 200 OK
        return ResponseEntity.ok(updatedSchedule);
    }

    // Delete a schedule by ID
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        // Delete the schedule using scheduleService
        scheduleService.deleteSchedule(id);

        // Broadcast the deleted schedule ID to subscribers (real-time update)
        scheduleBroadcaster.broadcastScheduleDeleted(id);

        // Return HTTP 200 OK with no content
        return ResponseEntity.ok().build();
    }

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