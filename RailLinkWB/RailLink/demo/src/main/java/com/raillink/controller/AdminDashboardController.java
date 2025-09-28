package com.raillink.controller;

import java.time.LocalDateTime;
import java.util.List;
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
import com.raillink.model.Role;
import com.raillink.model.Route;
import com.raillink.model.Schedule;
import com.raillink.model.Station;
import com.raillink.model.Train;
import com.raillink.model.User;
import com.raillink.service.AnnouncementService;
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

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return "admin/dashboard";
    }

    // Train Management
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
        return "admin/train-form";
    }

    @PostMapping("/trains/save")
    public String saveTrain(@ModelAttribute Train train) {
        trainService.saveTrain(train);
        return "redirect:/admin/trains";
    }

    @GetMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable Long id) {
        trainService.deleteTrain(id);
        return "redirect:/admin/trains";
    }

    // Station Management
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
    public String deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return "redirect:/admin/stations";
    }

    // Route Management
    @GetMapping("/routes")
    public String listRoutes(Model model) {
        List<Route> routes = routeService.findAllRoutes();
        model.addAttribute("routes", routes);
        return "admin/routes";
    }

    @GetMapping("/routes/add")
    public String showRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "admin/route-form";
    }

    @GetMapping("/routes/edit/{id}")
    public String editRoute(@PathVariable Long id, Model model) {
        Route route = routeService.findRouteById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        model.addAttribute("route", route);
        return "admin/route-form";
    }

    @PostMapping("/routes/save")
    public String saveRoute(@ModelAttribute Route route) {
        routeService.saveRoute(route);
        return "redirect:/admin/routes";
    }

    @GetMapping("/routes/delete/{id}")
    public String deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return "redirect:/admin/routes";
    }

    // Announcements UI (MVC pages)
    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", announcementService.findAll());
        return "admin/announcements";
    }

    @GetMapping("/announcements/add")
    public String addAnnouncement(Model model) {
        Announcement a = new Announcement();
        a.setStartDate(java.time.LocalDateTime.now());
        a.setEndDate(java.time.LocalDateTime.now().plusDays(1));
        model.addAttribute("announcement", a);
        return "admin/announcement-form";
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
            // Fallback to form with minimal context
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "admin/announcement-form";
        }
    }

    @GetMapping("/announcements/delete/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteById(id);
        return "redirect:/admin/announcements";
    }

    // Schedule Management
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
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trains);
        model.addAttribute("routes", routes);
        return "admin/schedule-form";
    }

    @GetMapping("/schedules/edit/{id}")
    public String editSchedule(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.findScheduleById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        List<Train> trains = trainService.findAllTrains();
        List<Route> routes = routeService.findAllRoutes();
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trains);
        model.addAttribute("routes", routes);
        return "admin/schedule-form";
    }

    @PostMapping("/schedules/save")
    public String saveSchedule(@ModelAttribute Schedule schedule, 
                             @RequestParam Long trainId, 
                             @RequestParam Long routeId) {
        Train train = trainService.findTrainById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found"));
        Route route = routeService.findRouteById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        schedule.setTrain(train);
        schedule.setRoute(route);
        scheduleService.saveSchedule(schedule);
        return "redirect:/admin/schedules";
    }

    @GetMapping("/schedules/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/admin/schedules";
    }

    // User Management
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
    public String saveUser(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam("role") String roleParam,
                           RedirectAttributes redirectAttributes) {
        try {
            Role role = mapToRole(roleParam);
            userService.registerUser(username, email, password, role);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("formUsername", username);
            redirectAttributes.addFlashAttribute("formEmail", email);
            redirectAttributes.addFlashAttribute("formRole", roleParam);
            return "redirect:/admin/users/add";
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
        // Fallback to passenger
        return Role.ROLE_PASSENGER;
    }

    @PostMapping("/users/update-role")
    public String updateUserRole(@RequestParam Long userId, @RequestParam Role role) {
        userService.updateUserRole(userId, role);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
} 