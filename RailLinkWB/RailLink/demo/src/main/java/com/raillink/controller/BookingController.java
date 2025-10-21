package com.raillink.controller;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.raillink.model.Booking;
import com.raillink.model.Schedule;
import com.raillink.model.Station;
import com.raillink.model.User;
import com.raillink.service.BookingService;
import com.raillink.service.PdfService;
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
    private PdfService pdfService;
    @GetMapping("/search-trains")
    public String searchTrains(Model model) {
        try {
            List<Station> stations = stationService.findAllStations();
            model.addAttribute("stations", stations);
            return "search-trains";
        } catch (Exception e) {
            System.err.println("Error loading search trains page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("stations", new java.util.ArrayList<Station>());
            model.addAttribute("error", "Unable to load stations. Please try again later.");
            return "search-trains";
        }
    }
    @GetMapping("/trains/search/results")
    public String searchResults(@RequestParam(required = false) String fromStation,
                               @RequestParam(required = false) String toStation,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false) String time,
                               @RequestParam(required = false) Long routeId,
                               @RequestParam(required = false, defaultValue = "ANY") String timeMode,
                               @RequestParam(required = false) String timeEnd,
                               Model model) {
        
        // Validate date - prevent searching for past dates
        if (date != null && !date.isBlank()) {
            try {
                java.time.LocalDate searchDate = java.time.LocalDate.parse(date);
                java.time.LocalDate today = java.time.LocalDate.now();
                
                if (searchDate.isBefore(today)) {
                    model.addAttribute("error", "You cannot search for trains on past dates. Please select today or a future date.");
                    model.addAttribute("schedules", new java.util.ArrayList<Schedule>());
                    model.addAttribute("fromStation", fromStation);
                    model.addAttribute("toStation", toStation);
                    model.addAttribute("date", date);
                    model.addAttribute("dateString", date);
                    model.addAttribute("time", time);
                    model.addAttribute("routeId", routeId);
                    model.addAttribute("timeMode", timeMode);
                    model.addAttribute("timeEnd", timeEnd);
                    return "search-results";
                }
            } catch (Exception e) {
                model.addAttribute("error", "Invalid date format. Please select a valid date.");
                model.addAttribute("schedules", new java.util.ArrayList<Schedule>());
                model.addAttribute("fromStation", fromStation);
                model.addAttribute("toStation", toStation);
                model.addAttribute("date", date);
                model.addAttribute("dateString", date);
                model.addAttribute("time", time);
                model.addAttribute("routeId", routeId);
                model.addAttribute("timeMode", timeMode);
                model.addAttribute("timeEnd", timeEnd);
                return "search-results";
            }
        }
        
        List<Schedule> schedules;
        if (routeId != null) {
            schedules = scheduleService.searchByRouteAndDate(routeId, date);
        } else {
            schedules = scheduleService.searchByStationsAndDate(fromStation, toStation, date);
        }
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
        java.time.LocalDate searchDate = null;
        if (date != null && !date.isBlank()) {
            try {
                searchDate = java.time.LocalDate.parse(date);
            } catch (Exception e) {
                searchDate = null;
            }
        }
        model.addAttribute("date", searchDate != null ? searchDate : date);
        model.addAttribute("dateString", date);
        model.addAttribute("time", time);
        model.addAttribute("routeId", routeId);
        model.addAttribute("timeMode", timeMode);
        model.addAttribute("timeEnd", timeEnd);
        return "search-results";
    }
    @GetMapping("/bookings/new/{scheduleId}")
    public String newBooking(@PathVariable Long scheduleId, 
                            Model model) {
        Schedule schedule = scheduleService.findScheduleById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        // Get train classes and capacities
        java.util.Map<String, Integer> trainClasses = schedule.getTrain().getClasses();
        
        // Get schedule pricing
        java.util.Map<String, java.math.BigDecimal> schedulePricing = schedule.getPricing();
        
        // Get available seats per class
        java.util.Map<String, Integer> availablePerClass = bookingService.getAvailableSeatsPerClass(schedule);
        
        // Build combined class information with pricing, capacity, and availability
        java.util.Map<String, java.util.Map<String, Object>> classInfo = new java.util.LinkedHashMap<>();
        if (trainClasses != null && !trainClasses.isEmpty()) {
            for (java.util.Map.Entry<String, Integer> entry : trainClasses.entrySet()) {
                String className = entry.getKey();
                Integer capacity = entry.getValue();
                
                java.util.Map<String, Object> info = new java.util.HashMap<>();
                info.put("capacity", capacity);
                
                // Add available seats for this class
                Integer available = availablePerClass.getOrDefault(className, capacity);
                info.put("available", available);
                
                // Get price from schedule pricing, or use default
                java.math.BigDecimal price;
                if (schedulePricing != null && schedulePricing.containsKey(className)) {
                    price = schedulePricing.get(className);
                } else {
                    // Default pricing based on class name
                    switch (className.toLowerCase()) {
                        case "first class":
                            price = java.math.BigDecimal.valueOf(1500.00);
                            break;
                        case "second class":
                            price = java.math.BigDecimal.valueOf(1000.00);
                            break;
                        case "third class":
                            price = java.math.BigDecimal.valueOf(500.00);
                            break;
                        default:
                            price = java.math.BigDecimal.valueOf(25.00);
                    }
                }
                info.put("price", price);
                
                classInfo.put(className, info);
            }
        }
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("availableSeats", bookingService.getAvailableSeatsForSchedule(schedule));
        model.addAttribute("classInfo", classInfo);
        return "booking-form";
    }
    @PostMapping("/bookings/create")
    public String createBooking(@RequestParam Long scheduleId,
                               @RequestParam(required = false) String seatNumber,
                               @RequestParam(required = false) List<String> seatNumbers,
                               @RequestParam(required = false) String ticketClass,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || "anonymousUser".equals(auth.getName()) || !auth.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to make a booking. Please login first.");
                return "redirect:/bookings/new/" + scheduleId;
            }
            String username = auth.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Schedule schedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            
            // Handle bookings - single ticket booking only
            List<Long> bookingIds = new ArrayList<>();
            
            // Handle multiple seat bookings (for backward compatibility)
            if (seatNumbers != null && !seatNumbers.isEmpty()) {
                for (String seat : seatNumbers) {
                    if (seat != null && !seat.trim().isEmpty()) {
                        Booking booking = bookingService.createBooking(user, schedule, seat.trim(), ticketClass);
                        bookingIds.add(booking.getId());
                    }
                }
            } 
            // Single seat booking
            else if (seatNumber != null && !seatNumber.trim().isEmpty()) {
                Booking booking = bookingService.createBooking(user, schedule, seatNumber, ticketClass);
                bookingIds.add(booking.getId());
            } else {
                // Auto-assign first available seat if no seat specified
                List<Integer> availableSeats = bookingService.getAvailableSeatsForSchedule(schedule);
                if (availableSeats.isEmpty()) {
                    throw new RuntimeException("No seats available for this schedule");
                }
                String seat = String.valueOf(availableSeats.get(0));
                Booking booking = bookingService.createBooking(user, schedule, seat, ticketClass);
                bookingIds.add(booking.getId());
            }
            
            if (bookingIds.isEmpty()) {
                throw new RuntimeException("No bookings were created");
            }
            
            // Redirect to booking success page
            return "redirect:/booking-success?bookingId=" + bookingIds.get(0) + "&count=1";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/new/" + scheduleId;
        }
    }
    @GetMapping("/booking-success")
    public String bookingSuccess(@RequestParam Long bookingId, 
                                @RequestParam(required = false, defaultValue = "1") Integer count,
                                Model model) {
        try {
            Booking mainBooking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Get all bookings for this user, schedule, and class made at the same time
            List<Booking> relatedBookings = new ArrayList<>();
            if (count > 1) {
                // Find bookings made around the same time (within 1 minute)
                List<Booking> userBookings = bookingService.findBookingsByUser(mainBooking.getUser().getId());
                java.time.LocalDateTime bookingTime = mainBooking.getBookingDate();
                
                for (Booking booking : userBookings) {
                    if (booking.getSchedule().getId().equals(mainBooking.getSchedule().getId()) &&
                        booking.getTicketClass() != null &&
                        booking.getTicketClass().equals(mainBooking.getTicketClass()) &&
                        "CONFIRMED".equals(booking.getStatus()) &&
                        Math.abs(java.time.Duration.between(booking.getBookingDate(), bookingTime).toSeconds()) < 60) {
                        relatedBookings.add(booking);
                    }
                }
            } else {
                relatedBookings.add(mainBooking);
            }
            
            // Calculate total fare and collect seat numbers
            java.math.BigDecimal totalFare = java.math.BigDecimal.ZERO;
            List<String> seatNumbers = new ArrayList<>();
            for (Booking booking : relatedBookings) {
                if (booking.getFare() != null) {
                    totalFare = totalFare.add(booking.getFare());
                }
                seatNumbers.add(booking.getSeatNumber());
            }
            
            model.addAttribute("booking", mainBooking);
            model.addAttribute("relatedBookings", relatedBookings);
            model.addAttribute("ticketCount", relatedBookings.size());
            model.addAttribute("totalFare", totalFare);
            model.addAttribute("seatNumbers", seatNumbers);
            model.addAttribute("ticketClass", mainBooking.getTicketClass());
            
            return "booking-success";
        } catch (Exception e) {
            model.addAttribute("error", "Booking not found");
            return "redirect:/trains/search";
        }
    }
    @GetMapping("/bookings/{bookingId}")
    public String viewBookingDetails(@PathVariable Long bookingId, Model model) {
        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Check if the current user owns this booking
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                User currentUser = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                if (!booking.getUser().getId().equals(currentUser.getId())) {
                    model.addAttribute("error", "You are not authorized to view this booking.");
                    return "error";
                }
            } else {
                model.addAttribute("error", "You must be logged in to view booking details.");
                return "error";
            }
            
            model.addAttribute("booking", booking);
            return "booking-details";
        } catch (Exception e) {
            model.addAttribute("error", "Booking not found or you don't have permission to view it.");
            return "error";
        }
    }

    @GetMapping("/my-bookings")
    public String myBookings(@RequestParam(required = false, defaultValue = "bookingDate") String sortBy,
                           @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                           @RequestParam(required = false) String statusFilter,
                           @RequestParam(required = false) String trainFilter,
                           @RequestParam(required = false) String routeFilter,
                           Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Booking> bookings = bookingService.findBookingsByUser(user.getId());
            
            // Apply filters
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                bookings = bookings.stream()
                        .filter(b -> b.getStatus().equalsIgnoreCase(statusFilter.trim()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            if (trainFilter != null && !trainFilter.trim().isEmpty()) {
                String filter = trainFilter.toLowerCase().trim();
                bookings = bookings.stream()
                        .filter(b -> b.getSchedule().getTrain().getName().toLowerCase().contains(filter))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            if (routeFilter != null && !routeFilter.trim().isEmpty()) {
                String filter = routeFilter.toLowerCase().trim();
                bookings = bookings.stream()
                        .filter(b -> b.getSchedule().getRoute().getName().toLowerCase().contains(filter))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply sorting
            switch (sortBy.toLowerCase()) {
                case "bookingdate":
                    bookings.sort((b1, b2) -> {
                        int result = b1.getBookingDate().compareTo(b2.getBookingDate());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                case "departuredate":
                    bookings.sort((b1, b2) -> {
                        int result = b1.getSchedule().getDepartureDate().compareTo(b2.getSchedule().getDepartureDate());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                case "train":
                    bookings.sort((b1, b2) -> {
                        int result = b1.getSchedule().getTrain().getName().compareToIgnoreCase(b2.getSchedule().getTrain().getName());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                case "route":
                    bookings.sort((b1, b2) -> {
                        int result = b1.getSchedule().getRoute().getName().compareToIgnoreCase(b2.getSchedule().getRoute().getName());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                case "status":
                    bookings.sort((b1, b2) -> {
                        int result = b1.getStatus().compareToIgnoreCase(b2.getStatus());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                case "fare":
                    bookings.sort((b1, b2) -> {
                        java.math.BigDecimal fare1 = b1.getFare() != null ? b1.getFare() : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal fare2 = b2.getFare() != null ? b2.getFare() : java.math.BigDecimal.ZERO;
                        int result = fare1.compareTo(fare2);
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
                    break;
                default:
                    // Default sorting by booking date
                    bookings.sort((b1, b2) -> {
                        int result = b1.getBookingDate().compareTo(b2.getBookingDate());
                        return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                    });
            }
            
            model.addAttribute("bookings", bookings);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortOrder", sortOrder);
            model.addAttribute("statusFilter", statusFilter);
            model.addAttribute("trainFilter", trainFilter);
            model.addAttribute("routeFilter", routeFilter);
            
            return "my-bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load bookings. Please try again later.");
            model.addAttribute("bookings", new java.util.ArrayList<Booking>());
            return "my-bookings";
        }
    }
    @GetMapping("/bookings/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }
    @GetMapping("/bookings/edit/{bookingId}")
    public String editBooking(@PathVariable Long bookingId, Model model) {
        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            List<Schedule> availableSchedules = scheduleService.findSchedulesByRoute(booking.getSchedule().getRoute().getId());
            model.addAttribute("booking", booking);
            model.addAttribute("availableSchedules", availableSchedules);
            return "edit-booking";
        } catch (Exception e) {
            return "redirect:/my-bookings";
        }
    }
    @PostMapping("/bookings/update")
    public String updateBooking(@RequestParam Long bookingId,
                               @RequestParam Long scheduleId,
                               @RequestParam String seatNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            Schedule newSchedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            if (!bookingService.isSeatAvailable(newSchedule, seatNumber)) {
                redirectAttributes.addFlashAttribute("error", "Seat " + seatNumber + " is not available for the selected schedule");
                return "redirect:/bookings/edit/" + bookingId;
            }
            booking.setSchedule(newSchedule);
            booking.setSeatNumber(seatNumber);
            bookingService.saveBooking(booking);
            redirectAttributes.addFlashAttribute("success", "Booking updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }
    @GetMapping("/bookings/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.deleteBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking deleted permanently from database");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }
    @GetMapping("/bookings/{bookingId}/ticket")
    public String downloadTicket(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        model.addAttribute("booking", booking);
        return "ticket";
    }
    @GetMapping("/bookings/{bookingId}/ticket/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Long bookingId,
                                                   @RequestParam(required = false, defaultValue = "1") Integer count) {
        try {
            Booking mainBooking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            byte[] bytes;
            String filename;

            if (count != null && count > 1) {
                // Build related bookings list similar to booking-success
                List<Booking> relatedBookings = new ArrayList<>();
                List<Booking> userBookings = bookingService.findBookingsByUser(mainBooking.getUser().getId());
                java.time.LocalDateTime bookingTime = mainBooking.getBookingDate();
                for (Booking b : userBookings) {
                    if (b.getSchedule().getId().equals(mainBooking.getSchedule().getId()) &&
                        b.getTicketClass() != null &&
                        b.getTicketClass().equals(mainBooking.getTicketClass()) &&
                        "CONFIRMED".equals(b.getStatus()) &&
                        Math.abs(java.time.Duration.between(b.getBookingDate(), bookingTime).toSeconds()) < 60) {
                        relatedBookings.add(b);
                    }
                }
                if (relatedBookings.isEmpty()) {
                    relatedBookings.add(mainBooking);
                }
                bytes = pdfService.generateTicketsPdf(relatedBookings);
                filename = "tickets_" + bookingId + "_" + relatedBookings.size() + ".txt";
            } else {
                bytes = pdfService.generateTicketPdf(mainBooking);
                filename = "ticket_" + bookingId + ".txt";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 