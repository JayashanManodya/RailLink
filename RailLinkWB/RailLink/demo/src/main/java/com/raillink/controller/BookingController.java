package com.raillink.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raillink.model.Booking;
import com.raillink.model.Refund;
import com.raillink.model.Schedule;
import com.raillink.model.Station;
import com.raillink.model.User;
import com.raillink.service.BookingService;
import com.raillink.service.RefundService;
import com.raillink.service.ScheduleService;
import com.raillink.service.StationService;
import com.raillink.service.UserService;

@Controller
public class BookingController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private StationService stationService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private RefundService refundService;

    // Search trains page
    @GetMapping("/trains/search")
    public String searchTrains(Model model) {
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        return "search-trains";
    }

    // Search results
    @GetMapping("/trains/search/results")
    public String searchResults(@RequestParam(required = false) String fromStation,
                               @RequestParam(required = false) String toStation,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false) String time,
                               @RequestParam(required = false) Long routeId,
                               @RequestParam(required = false, defaultValue = "ANY") String timeMode,
                               @RequestParam(required = false) String timeEnd,
                               Model model) {
        List<Schedule> schedules;
        if (routeId != null) {
            schedules = scheduleService.searchByRouteAndDate(routeId, date);
        } else {
            schedules = scheduleService.searchByStationsAndDate(fromStation, toStation, date);
        }
        // Enhanced time filtering
        final String mode = timeMode != null ? timeMode.toUpperCase() : "ANY";
        if (!"ANY".equals(mode)) {
            try {
                final java.time.LocalTime parsedStart = (time != null && !time.isBlank()) ? java.time.LocalTime.parse(time) : null;
                final java.time.LocalTime parsedEnd = (timeEnd != null && !timeEnd.isBlank()) ? java.time.LocalTime.parse(timeEnd) : null;
                // Pre-compute normalized range or around bounds
                final java.time.LocalTime aroundMin = ("AROUND".equals(mode) && parsedStart != null) ? parsedStart.minusMinutes(30) : null;
                final java.time.LocalTime aroundMax = ("AROUND".equals(mode) && parsedStart != null) ? parsedStart.plusMinutes(30) : null;
                java.time.LocalTime tmpStart = parsedStart;
                java.time.LocalTime tmpEnd = parsedEnd;
                if ("RANGE".equals(mode) && tmpStart != null && tmpEnd != null && tmpEnd.isBefore(tmpStart)) {
                    // swap to normalize
                    java.time.LocalTime t = tmpStart; tmpStart = tmpEnd; tmpEnd = t;
                }
                final java.time.LocalTime rangeStart = tmpStart;
                final java.time.LocalTime rangeEnd = tmpEnd;

                schedules = schedules.stream().filter(s -> {
                    java.time.LocalTime dep = s.getDepartureDate().toLocalTime();
                    switch (mode) {
                        case "EXACT":
                            return parsedStart != null && dep.getHour() == parsedStart.getHour() && dep.getMinute() == parsedStart.getMinute();
                        case "AROUND":
                            if (aroundMin == null || aroundMax == null) return true;
                            return !dep.isBefore(aroundMin) && !dep.isAfter(aroundMax);
                        case "RANGE":
                            if (rangeStart == null || rangeEnd == null) return true;
                            return !dep.isBefore(rangeStart) && !dep.isAfter(rangeEnd);
                        default:
                            return true;
                    }
                }).toList();
            } catch (Exception ignored) {}
        } else if (time != null && !time.isBlank()) {
            // Backward compatible: if time provided but no timeMode, keep EXACT behavior
            try {
                java.time.LocalTime t = java.time.LocalTime.parse(time);
                schedules = schedules.stream()
                        .filter(s -> s.getDepartureDate().toLocalTime().getHour() == t.getHour()
                                && s.getDepartureDate().toLocalTime().getMinute() == t.getMinute())
                        .toList();
            } catch (Exception ignored) {}
        }
        model.addAttribute("schedules", schedules);
        model.addAttribute("fromStation", fromStation);
        model.addAttribute("toStation", toStation);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("routeId", routeId);
        model.addAttribute("timeMode", timeMode);
        model.addAttribute("timeEnd", timeEnd);
        return "search-results";
    }

    // New booking form
    @GetMapping("/bookings/new/{scheduleId}")
    public String newBooking(@PathVariable Long scheduleId, Model model) {
        Schedule schedule = scheduleService.findScheduleById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        model.addAttribute("schedule", schedule);
        model.addAttribute("availableSeats", bookingService.getAvailableSeatsForSchedule(schedule));
        return "booking-form";
    }

    // Create booking
    @PostMapping("/bookings/create")
    public String createBooking(@RequestParam Long scheduleId,
                               @RequestParam String seatNumber,
                               Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Schedule schedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            Booking booking = bookingService.createBooking(user, schedule, seatNumber);
            return "redirect:/my-bookings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/bookings/new/" + scheduleId;
        }
    }

    // My bookings page
    @GetMapping("/my-bookings")
    public String myBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingService.findBookingsByUser(user.getId());
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    // Cancel booking
    @GetMapping("/bookings/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId) {
        Booking cancelled = bookingService.cancelBooking(bookingId);
        try {
            java.math.BigDecimal amount = cancelled.getFare() != null ? cancelled.getFare() : new java.math.BigDecimal("0");
            refundService.requestRefund(cancelled.getId(), amount, "Booking cancelled");
        } catch (Exception ignored) {}
        return "redirect:/my-bookings";
    }

    // Request refund (basic flow)
    @PostMapping("/bookings/{bookingId}/refund")
    @ResponseBody
    public Map<String, Object> requestRefund(@PathVariable Long bookingId,
                                             @RequestParam(required = false) String reason,
                                             @RequestParam(required = false) String amount) {
        try {
            java.math.BigDecimal amt = amount != null ? new java.math.BigDecimal(amount) : new java.math.BigDecimal("0");
            Refund refund = refundService.requestRefund(bookingId, amt, reason);
            return Map.of("message", "Refund requested", "refundId", refund.getId());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Download ticket
    @GetMapping("/bookings/{bookingId}/ticket")
    public String downloadTicket(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        model.addAttribute("booking", booking);
        return "ticket";
    }

    // API endpoints for AJAX calls
    @GetMapping("/api/bookings/search")
    @ResponseBody
    public List<Schedule> searchSchedules(@RequestParam(required = false) String startStation,
                                         @RequestParam(required = false) String endStation,
                                          @RequestParam(required = false) String date,
                                          @RequestParam(required = false) String time,
                                         @RequestParam(required = false) Long routeId,
                                         @RequestParam(required = false, defaultValue = "ANY") String timeMode,
                                         @RequestParam(required = false) String timeEnd) {
        try {
            java.util.List<Schedule> res = (routeId != null)
                    ? scheduleService.searchByRouteAndDate(routeId, date)
                    : scheduleService.searchByStationsAndDate(startStation, endStation, date);
            final String mode = timeMode != null ? timeMode.toUpperCase() : "ANY";
            if (!"ANY".equals(mode)) {
                try {
                    final java.time.LocalTime parsedStart = (time != null && !time.isBlank()) ? java.time.LocalTime.parse(time) : null;
                    final java.time.LocalTime parsedEnd = (timeEnd != null && !timeEnd.isBlank()) ? java.time.LocalTime.parse(timeEnd) : null;
                    final java.time.LocalTime aroundMin = ("AROUND".equals(mode) && parsedStart != null) ? parsedStart.minusMinutes(30) : null;
                    final java.time.LocalTime aroundMax = ("AROUND".equals(mode) && parsedStart != null) ? parsedStart.plusMinutes(30) : null;
                    java.time.LocalTime tmpStart = parsedStart;
                    java.time.LocalTime tmpEnd = parsedEnd;
                    if ("RANGE".equals(mode) && tmpStart != null && tmpEnd != null && tmpEnd.isBefore(tmpStart)) {
                        java.time.LocalTime t = tmpStart; tmpStart = tmpEnd; tmpEnd = t;
                    }
                    final java.time.LocalTime rangeStart = tmpStart;
                    final java.time.LocalTime rangeEnd = tmpEnd;

                    res = res.stream().filter(s -> {
                        java.time.LocalTime dep = s.getDepartureDate().toLocalTime();
                        switch (mode) {
                            case "EXACT":
                                return parsedStart != null && dep.getHour() == parsedStart.getHour() && dep.getMinute() == parsedStart.getMinute();
                            case "AROUND":
                                if (aroundMin == null || aroundMax == null) return true;
                                return !dep.isBefore(aroundMin) && !dep.isAfter(aroundMax);
                            case "RANGE":
                                if (rangeStart == null || rangeEnd == null) return true;
                                return !dep.isBefore(rangeStart) && !dep.isAfter(rangeEnd);
                            default:
                                return true;
                        }
                    }).toList();
                } catch (Exception ignored) {}
            } else if (time != null && !time.isBlank()) {
                try {
                    java.time.LocalTime t = java.time.LocalTime.parse(time);
                    res = res.stream().filter(s -> s.getDepartureDate().toLocalTime().getHour() == t.getHour()
                            && s.getDepartureDate().toLocalTime().getMinute() == t.getMinute()).toList();
                } catch (Exception ignored) {}
            }
            return res;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/api/bookings/")
    @ResponseBody
    public Map<String, Object> createBookingApi(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long scheduleId = Long.valueOf(request.get("scheduleId").toString());
            String seatNumber = request.get("seatNumber").toString();

            Schedule schedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            Booking booking = bookingService.createBooking(user, schedule, seatNumber);

            return Map.of("message", "Booking created successfully", "bookingId", booking.getId());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // Removed duplicate API mapping for "/api/profile/my-bookings".
} 