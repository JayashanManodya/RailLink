package com.raillink.controller;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.raillink.model.Announcement;
import com.raillink.model.Booking;
import com.raillink.model.Role;
import com.raillink.model.Route;
import com.raillink.model.Schedule;
import com.raillink.model.Station;
import com.raillink.model.Train;
import com.raillink.model.User;
import com.raillink.service.AnnouncementService;
import com.raillink.service.BookingService;
import com.raillink.service.RouteService;
import com.raillink.service.ScheduleService;
import com.raillink.service.StationService;
import com.raillink.service.TrainService;
import com.raillink.service.UserService;
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    @Autowired
    private TrainService trainService;
    @Autowired
    private StationService stationService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private AnnouncementService announcementService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> users = userService.findAllUsers();
        List<Train> trains = trainService.findAllTrains();
        List<Station> stations = stationService.findAllStations();
        List<Route> routes = routeService.findAllRoutes();
        List<Booking> bookings = bookingService.findAllBookings();
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalTrains", trains.size());
        model.addAttribute("totalStations", stations.size());
        model.addAttribute("totalRoutes", routes.size());
        model.addAttribute("totalBookings", bookings.size());
        List<Booking> recentBookings = bookings.stream()
                .sorted((b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()))
                .limit(5)
                .toList();
        model.addAttribute("recentBookings", recentBookings);
        return "admin/dashboard";
    }
    @GetMapping("/trains")
    public String listTrains(Model model) {
        List<Train> trains = trainService.findAllTrains();
        model.addAttribute("trains", trains);
        return "admin/trains";
    }
    @GetMapping("/trains/add")
    public String showTrainForm(Model model) {
        model.addAttribute("train", new Train());
        return "admin/train-form";
    }
    @GetMapping("/trains/edit/{id}")
    public String editTrain(@PathVariable Long id, Model model) {
        Train train = trainService.findTrainById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));
        model.addAttribute("train", train);
        Map<String, Integer> existingClasses = train.getClasses();
        if (existingClasses != null && !existingClasses.isEmpty()) {
            model.addAttribute("existingClasses", existingClasses);
        }
        return "admin/train-form";
    }
    @PostMapping("/trains/save")
    public String saveTrain(@ModelAttribute Train train,
                           @RequestParam(value = "classNames", required = false) List<String> classNames,
                           @RequestParam(value = "classCapacities", required = false) List<Integer> classCapacities) {
        if (classNames != null && classCapacities != null && !classNames.isEmpty()) {
            Map<String, Integer> classes = new HashMap<>();
            for (int i = 0; i < classNames.size() && i < classCapacities.size(); i++) {
                String className = classNames.get(i);
                Integer capacity = classCapacities.get(i);
                if (className != null && !className.trim().isEmpty() && capacity != null && capacity > 0) {
                    classes.put(className.trim(), capacity);
                }
            }
            train.setClasses(classes);
        }
        trainService.saveTrain(train);
        return "redirect:/admin/trains";
    }
    @GetMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return "redirect:/admin/trains";
    }
    @GetMapping("/stations")
    public String listStations(Model model) {
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        return "admin/stations";
    }
    @GetMapping("/stations/add")
    public String showStationForm(Model model) {
        model.addAttribute("station", new Station());
        return "admin/station-form";
    }
    @GetMapping("/stations/edit/{id}")
    public String editStation(@PathVariable Long id, Model model) {
        Station station = stationService.findStationById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        model.addAttribute("station", station);
        return "admin/station-form";
    }
    @PostMapping("/stations/save")
    public String saveStation(@ModelAttribute Station station) {
        stationService.saveStation(station);
        return "redirect:/admin/stations";
    }
    @GetMapping("/stations/delete/{id}")
    public String deleteStation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            stationService.deleteStation(id);
            redirectAttributes.addFlashAttribute("success", "Station deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting station: " + e.getMessage());
        }
        return "redirect:/admin/stations";
    }
    @GetMapping("/routes")
    public String listRoutes(Model model) {
        List<Route> routes = routeService.findAllRoutes();
        model.addAttribute("routes", routes);
        return "admin/routes";
    }
    @GetMapping("/routes/add")
    public String showRouteForm(Model model) {
        model.addAttribute("route", new Route());
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        return "admin/route-form";
    }
    @GetMapping("/routes/edit/{id}")
    public String editRoute(@PathVariable Long id, Model model) {
        Route route = routeService.findRouteById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        model.addAttribute("route", route);
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        if (route.getPath() != null && !route.getPath().trim().isEmpty()) {
            List<String> selectedStationIds = routeService.convertPathToStationIds(route.getPath())
                .stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("selectedStationIds", selectedStationIds);
        }
        return "admin/route-form";
    }
    @PostMapping("/routes/save")
    public String saveRoute(@ModelAttribute Route route, 
                           @RequestParam(value = "stationIds", required = false) List<Long> stationIds,
                           @RequestParam(value = "orderedStationIds", required = false) String orderedStationIds) {
        if (orderedStationIds != null && !orderedStationIds.trim().isEmpty()) {
            try {
                route.setPath(orderedStationIds);
            } catch (Exception e) {
                System.err.println("Error saving ordered station path: " + e.getMessage());
            }
        } else if (stationIds != null && !stationIds.isEmpty()) {
            try {
                String path = routeService.convertStationIdsToPath(stationIds);
                route.setPath(path);
            } catch (Exception e) {
                System.err.println("Error saving station path: " + e.getMessage());
            }
        }
        routeService.saveRoute(route);
        return "redirect:/admin/routes";
    }
    @GetMapping("/routes/delete/{id}")
    public String deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return "redirect:/admin/routes";
    }
    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", announcementService.findAll());
        return "admin/announcements";
    }
    @GetMapping("/announcements/add")
    public String addAnnouncement(Model model) {
        try {
            Announcement announcement = new Announcement();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            announcement.setStartDate(now);
            announcement.setEndDate(now.plusDays(30));
            model.addAttribute("announcement", announcement);
            return "admin/announcement-form";
        } catch (Exception e) {
            System.err.println("Error in addAnnouncement: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading announcement form: " + e.getMessage());
            return "redirect:/admin/announcements";
        }
    }
    @GetMapping("/announcements/edit/{id}")
    public String editAnnouncement(@PathVariable Long id, Model model) {
        Announcement a = announcementService.findById(id).orElseThrow(() -> new RuntimeException("Announcement not found"));
        model.addAttribute("announcement", a);
        return "admin/announcement-form";
    }
    @PostMapping("/announcements/save")
    public String saveAnnouncement(@RequestParam(required = false) Long id,
                                   @RequestParam String title,
                                   @RequestParam(required = false) String message,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   Authentication authentication,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            java.time.LocalDateTime start = (startDate != null && !startDate.isBlank()) ? java.time.LocalDateTime.parse(startDate, fmt) : java.time.LocalDateTime.now();
            java.time.LocalDateTime end = (endDate != null && !endDate.isBlank()) ? java.time.LocalDateTime.parse(endDate, fmt) : start.plusDays(1);
            if (end.isBefore(start)) end = start.plusDays(1);
            Announcement a = new Announcement();
            if (id != null) a.setId(id);
            a.setTitle(title);
            a.setMessage(message);
            a.setStartDate(start);
            a.setEndDate(end);
            if (authentication != null) a.setAuthor(authentication.getName());
            announcementService.save(a);
            redirectAttributes.addFlashAttribute("success", "Announcement saved successfully");
            return "redirect:/admin/announcements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "admin/announcement-form";
        }
    }
    @GetMapping("/announcements/delete/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteById(id);
        return "redirect:/admin/announcements";
    }
    @GetMapping("/schedules")
    public String listSchedules(Model model) {
        List<Schedule> schedules = scheduleService.findAllSchedules();
        model.addAttribute("schedules", schedules);
        return "admin/schedules";
    }
    @GetMapping("/schedules/add")
    public String showScheduleForm(Model model) {
        Schedule schedule = new Schedule();
        schedule.setDepartureDate(LocalDateTime.now());
        schedule.setArrivalDate(LocalDateTime.now().plusHours(1));
        schedule.setStatus("ON_TIME");
        List<Train> trains = trainService.findAllTrains();
        List<Route> routes = routeService.findAllRoutes();
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trains);
        model.addAttribute("routes", routes);
        model.addAttribute("stations", stations);
        return "admin/schedule-form";
    }
    @GetMapping("/schedules/edit/{id}")
    public String editSchedule(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.findScheduleById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        List<Train> trains = trainService.findAllTrains();
        List<Route> routes = routeService.findAllRoutes();
        List<Station> stations = stationService.findAllStations();
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trains);
        model.addAttribute("routes", routes);
        model.addAttribute("stations", stations);
        return "admin/schedule-form";
    }
    @PostMapping("/schedules/save")
    public String saveSchedule(@ModelAttribute Schedule schedule, 
                             @RequestParam Long trainId, 
                             @RequestParam Long routeId,
                             @RequestParam(required = false) String scheduleName,
                             @RequestParam(required = false) Integer delayMinutes,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(required = false) Boolean dailySchedule,
                             @RequestParam(required = false) List<Long> stationIds,
                             @RequestParam(required = false) List<String> arrivalTimes,
                             @RequestParam(required = false) List<String> departureTimes,
                             @RequestParam(required = false) List<String> platforms,
                             @RequestParam(required = false) List<String> classPrices,
                             @RequestParam(required = false) List<String> classNames,
                             RedirectAttributes redirectAttributes) {
        try {
            Train train = trainService.findTrainById(trainId)
                    .orElseThrow(() -> new RuntimeException("Train not found"));
            Route route = routeService.findRouteById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            schedule.setTrain(train);
            schedule.setRoute(route);
            schedule.setScheduleName(scheduleName);
            schedule.setDelayMinutes(delayMinutes);
            if (classPrices != null && classNames != null && !classPrices.isEmpty()) {
                java.util.Map<String, java.math.BigDecimal> pricing = new java.util.HashMap<>();
                for (int i = 0; i < Math.min(classPrices.size(), classNames.size()); i++) {
                    if (classPrices.get(i) != null && !classPrices.get(i).trim().isEmpty() && 
                        classNames.get(i) != null && !classNames.get(i).trim().isEmpty()) {
                        try {
                            java.math.BigDecimal price = new java.math.BigDecimal(classPrices.get(i));
                            pricing.put(classNames.get(i), price);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid price format: " + classPrices.get(i));
                        }
                    }
                }
                schedule.setPricing(pricing);
            }
            if (dailySchedule != null && dailySchedule && startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    Schedule dailyScheduleInstance = new Schedule();
                    dailyScheduleInstance.setTrain(train);
                    dailyScheduleInstance.setRoute(route);
                    dailyScheduleInstance.setScheduleName(scheduleName);
                    dailyScheduleInstance.setStatus(schedule.getStatus());
                    dailyScheduleInstance.setDelayMinutes(delayMinutes);
                    if (schedule.getPricingJson() != null) {
                        dailyScheduleInstance.setPricingJson(schedule.getPricingJson());
                    }
                    LocalDateTime depTime = schedule.getDepartureDate();
                    LocalDateTime arrTime = schedule.getArrivalDate();
                    LocalDateTime newDep = date.atTime(depTime.toLocalTime());
                    LocalDateTime newArr = date.atTime(arrTime.toLocalTime());
                    
                    // If arrival time is before departure time, it means the train arrives the next day
                    if (newArr.isBefore(newDep) || newArr.isEqual(newDep)) {
                        newArr = newArr.plusDays(1);
                    }
                    
                    dailyScheduleInstance.setDepartureDate(newDep);
                    dailyScheduleInstance.setArrivalDate(newArr);
                    scheduleService.saveSchedule(dailyScheduleInstance);
                }
                redirectAttributes.addFlashAttribute("success", "Daily schedules created successfully");
            } else {
                scheduleService.saveSchedule(schedule);
                redirectAttributes.addFlashAttribute("success", "Schedule saved successfully");
            }
            return "redirect:/admin/schedules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving schedule: " + e.getMessage());
            return "redirect:/admin/schedules/add";
        }
    }
    @GetMapping("/schedules/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/admin/schedules";
    }
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "q", required = false) String query, Model model) {
        List<User> users = userService.findAllUsers();
        if (query != null && !query.isBlank()) {
            String lower = query.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(lower)
                            || u.getEmail().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }
    @GetMapping("/users/add")
    public String showUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }
    @PostMapping("/users/save")
    public String saveUser(@RequestParam(required = false) Long userId,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam(required = false) String password,
                           @RequestParam("role") String roleParam,
                           RedirectAttributes redirectAttributes) {
        try {
            Role role = mapToRole(roleParam);
            if (userId != null) {
                User existingUser = userService.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                existingUser.setUsername(username);
                existingUser.setEmail(email);
                existingUser.setRole(role);
                if (password != null && !password.trim().isEmpty()) {
                    existingUser.setPassword(password);
                }
                userService.updateUser(existingUser);
                redirectAttributes.addFlashAttribute("success", "User updated successfully");
            } else {
                if (password == null || password.trim().isEmpty()) {
                    throw new RuntimeException("Password is required for new users");
                }
                userService.registerUser(username, email, password, role);
                redirectAttributes.addFlashAttribute("success", "User created successfully");
            }
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("formUsername", username);
            redirectAttributes.addFlashAttribute("formEmail", email);
            redirectAttributes.addFlashAttribute("formRole", roleParam);
            if (userId != null) {
                redirectAttributes.addFlashAttribute("userId", userId);
                return "redirect:/admin/users/edit/" + userId;
            } else {
                return "redirect:/admin/users/add";
            }
        }
    }
    private Role mapToRole(String roleParam) {
        if (roleParam == null) return Role.ROLE_PASSENGER;
        String val = roleParam.trim();
        if (val.isEmpty()) return Role.ROLE_PASSENGER;
        try {
            if (val.startsWith("ROLE_")) {
                return Role.valueOf(val);
            }
        } catch (IllegalArgumentException ignored) {}
        String upper = val.toUpperCase();
        if ("ADMIN".equals(upper)) return Role.ROLE_ADMIN;
        if ("STAFF".equals(upper)) return Role.ROLE_STAFF;
        if ("PASSENGER".equals(upper)) return Role.ROLE_PASSENGER;
        return Role.ROLE_PASSENGER;
    }
    @PostMapping("/users/update-role")
    public String updateUserRole(@RequestParam Long userId, @RequestParam Role role) {
        userService.updateUserRole(userId, role);
        return "redirect:/admin/users";
    }
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("formUsername", user.getUsername());
        model.addAttribute("formEmail", user.getEmail());
        model.addAttribute("formRole", user.getRole().toString());
        model.addAttribute("userId", user.getId());
        return "admin/user-form";
    }
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/bookings")
    public String listBookings(@RequestParam(required = false) String passenger,
                              @RequestParam(required = false) String bookingId,
                              Model model) {
        System.out.println("=== ADMIN BOOKINGS PAGE LOADED ===");
        System.out.println("Search parameters - Passenger: " + passenger + ", Booking ID: " + bookingId);
        
        List<Booking> bookings;
        
        // Apply search filters
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            // Search by booking ID
            try {
                Long id = Long.parseLong(bookingId.trim());
                Optional<Booking> booking = bookingService.findBookingById(id);
                bookings = booking.map(List::of).orElse(List.of());
                System.out.println("Searching by booking ID: " + id + ", Found: " + bookings.size());
            } catch (NumberFormatException e) {
                System.out.println("Invalid booking ID format: " + bookingId);
                bookings = List.of();
            }
        } else if (passenger != null && !passenger.trim().isEmpty()) {
            // Search by passenger name or email
            bookings = bookingService.findBookingsByPassenger(passenger.trim());
            System.out.println("Searching by passenger: " + passenger + ", Found: " + bookings.size());
        } else {
            // No search criteria, return all bookings
            bookings = bookingService.findAllBookings();
            System.out.println("No search criteria, returning all bookings: " + bookings.size());
        }
        
        for (Booking booking : bookings) {
            System.out.println("Booking ID: " + booking.getId() + 
                             ", Status: " + booking.getStatus() + 
                             ", User: " + booking.getUser().getUsername());
        }
        
        // Add users and schedules for create booking form
        List<User> users = userService.findAllUsers();
        List<Schedule> schedules = scheduleService.findAllSchedules();
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("users", users);
        model.addAttribute("schedules", schedules);
        return "admin/bookings";
    }
    @GetMapping("/bookings/edit/{id}")
    public String editBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingService.findBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        List<Schedule> availableSchedules = scheduleService.findSchedulesByRoute(booking.getSchedule().getRoute().getId());
        model.addAttribute("booking", booking);
        model.addAttribute("availableSchedules", availableSchedules);
        return "admin/booking-edit-form";
    }
    @PostMapping("/bookings/update")
    public String updateBooking(@RequestParam Long bookingId,
                               @RequestParam Long scheduleId,
                               @RequestParam String seatNumber,
                               @RequestParam(required = false) String status,
                               RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            Schedule newSchedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            booking.setSchedule(newSchedule);
            booking.setSeatNumber(seatNumber);
            if (status != null && !status.isEmpty()) {
                booking.setStatus(status);
            }
            bookingService.saveBooking(booking);
            redirectAttributes.addFlashAttribute("success", "Booking updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
    
    @PostMapping("/bookings/create")
    public String createBooking(@RequestParam Long passengerId,
                               @RequestParam Long scheduleId,
                               @RequestParam String seatNumber,
                               @RequestParam(required = false) String ticketClass,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(passengerId)
                    .orElseThrow(() -> new RuntimeException("Passenger not found"));
            Schedule schedule = scheduleService.findScheduleById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            
            // Create the booking
            Booking booking = bookingService.createBooking(user, schedule, seatNumber, ticketClass);
            booking.setStatus(status);
            bookingService.saveBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", "Booking created successfully for " + user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
    
    @GetMapping("/bookings/change-passenger/{id}")
    public String changePassenger(@PathVariable Long id,
                                 @RequestParam Long newPassengerId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.findBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            User newUser = userService.findById(newPassengerId)
                    .orElseThrow(() -> new RuntimeException("New passenger not found"));
            
            String oldPassenger = booking.getUser().getUsername();
            booking.setUser(newUser);
            bookingService.saveBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", 
                "Passenger changed from " + oldPassenger + " to " + newUser.getUsername() + " for booking #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing passenger: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
    
    @GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Attempting to cancel booking with ID: " + id);
            bookingService.cancelBooking(id);
            System.out.println("Booking cancelled successfully");
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
    @GetMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("=== ADMIN DELETE BOOKING ENDPOINT CALLED ===");
        System.out.println("Received request to delete booking with ID: " + id);
        System.out.println("ID type: " + id.getClass().getSimpleName());
        try {
            System.out.println("Attempting to permanently delete booking with ID: " + id);
            boolean exists = bookingService.findBookingById(id).isPresent();
            System.out.println("Booking exists: " + exists);
            if (!exists) {
                System.out.println("Booking not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Booking not found with ID: " + id);
                return "redirect:/admin/bookings";
            }
            System.out.println("Deleting booking from database");
            bookingService.deleteBooking(id);
            System.out.println("Booking deleted successfully from database");
            redirectAttributes.addFlashAttribute("success", "Booking deleted permanently from database");
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
    @GetMapping("/bookings/create-test")
    public String createTestBooking(RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.findAllUsers();
            if (users.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No users found");
                return "redirect:/admin/bookings";
            }
            List<Schedule> schedules = scheduleService.findAllSchedules();
            if (schedules.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No schedules found");
                return "redirect:/admin/bookings";
            }
            User user = users.get(0);
            Schedule schedule = schedules.get(0);
            Booking testBooking = bookingService.createBooking(user, schedule, "A1");
            redirectAttributes.addFlashAttribute("success", "Test booking created with ID: " + testBooking.getId());
        } catch (Exception e) {
            System.err.println("Error creating test booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating test booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
} 