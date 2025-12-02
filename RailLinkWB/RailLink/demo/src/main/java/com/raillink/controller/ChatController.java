package com.raillink.controller;

import com.raillink.dto.ChatMessageRequest;
import com.raillink.dto.ChatResponse;
import com.raillink.model.Announcement;
import com.raillink.model.Booking;
import com.raillink.model.Schedule;
import com.raillink.model.User;
import com.raillink.service.AnnouncementService;
import com.raillink.service.BookingService;
import com.raillink.service.N8nChatService;
import com.raillink.service.UserService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides a lightweight proxy endpoint that forwards chat events to n8n while attaching
 * contextual data fetched from the application's database.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final N8nChatService chatService;
    private final UserService userService;
    private final BookingService bookingService;
    private final AnnouncementService announcementService;

    public ChatController(
        N8nChatService chatService,
        UserService userService,
        BookingService bookingService,
        AnnouncementService announcementService
    ) {
        this.chatService = chatService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.announcementService = announcementService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
        @RequestBody ChatMessageRequest request,
        Authentication authentication
    ) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return ResponseEntity.badRequest()
                .body(ChatResponse.error(null, "Please provide a prompt before sending a message."));
        }

        Map<String, Object> context = buildContext(authentication);
        ChatResponse response = chatService.sendMessage(request, context);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildContext(Authentication authentication) {
        Map<String, Object> context = new HashMap<>();
        context.put("timestamp", Instant.now().toString());
        context.put("activeAnnouncements", announcementService.findActiveAnnouncements()
            .stream()
            .map(this::announcementSummary)
            .toList());

        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            context.put("authenticated", false);
            return context;
        }

        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        if (userOptional.isEmpty()) {
            context.put("authenticated", false);
            return context;
        }

        User user = userOptional.get();
        context.put("authenticated", true);
        context.put("userProfile", userSummary(user));

        List<Booking> bookings = bookingService.findBookingsByUserWithDetails(user.getId());
        context.put("bookings", bookings.stream().map(this::bookingSummary).toList());
        context.put("bookingStats", buildBookingStats(bookings));

        return context;
    }

    private Map<String, Object> userSummary(User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole().name());
        profile.put("emailNotifications", user.isEmailNotifications());
        profile.put("privacyPolicyAccepted", user.isPrivacyPolicyAccepted());
        return profile;
    }

    private Map<String, Object> bookingSummary(Booking booking) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", booking.getId());
        summary.put("status", booking.getStatus());
        summary.put("seatNumber", booking.getSeatNumber());
        summary.put("ticketClass", booking.getTicketClass());
        summary.put("fare", booking.getFare() != null ? booking.getFare().toPlainString() : null);
        summary.put("bookingDate", booking.getBookingDate() != null ? booking.getBookingDate().toString() : null);

        Schedule schedule = booking.getSchedule();
        if (schedule != null) {
            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("id", schedule.getId());
            scheduleInfo.put("departureDate", schedule.getDepartureDate() != null ? schedule.getDepartureDate().toString() : null);
            scheduleInfo.put("arrivalDate", schedule.getArrivalDate() != null ? schedule.getArrivalDate().toString() : null);
            scheduleInfo.put("status", schedule.getStatus());
            scheduleInfo.put("duration", schedule.getDuration());
            if (schedule.getRoute() != null) {
                scheduleInfo.put("routeName", schedule.getRoute().getName());
                scheduleInfo.put("routeCode", schedule.getRoute().getRouteCode());
            }
            if (schedule.getTrain() != null) {
                scheduleInfo.put("trainName", schedule.getTrain().getName());
            }
            summary.put("schedule", scheduleInfo);
        }

        return summary;
    }

    private Map<String, Object> announcementSummary(Announcement announcement) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", announcement.getId());
        summary.put("title", announcement.getTitle());
        summary.put("message", announcement.getMessage());
        summary.put("startDate", announcement.getStartDate() != null ? announcement.getStartDate().toString() : null);
        summary.put("endDate", announcement.getEndDate() != null ? announcement.getEndDate().toString() : null);
        summary.put("author", announcement.getAuthor());
        return summary;
    }

    private Map<String, Object> buildBookingStats(List<Booking> bookings) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", bookings.size());

        long confirmed = bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count();
        long cancelled = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();
        long completed = bookings.stream().filter(b -> "COMPLETED".equalsIgnoreCase(b.getStatus())).count();

        stats.put("confirmed", confirmed);
        stats.put("cancelled", cancelled);
        stats.put("completed", completed);

        List<Booking> upcoming = bookings.stream()
            .filter(b -> b.getSchedule() != null && b.getSchedule().getDepartureDate() != null)
            .filter(b -> {
                LocalDateTime departure = b.getSchedule().getDepartureDate();
                return departure.isAfter(LocalDateTime.now());
            })
            .sorted(Comparator.comparing(b -> b.getSchedule().getDepartureDate()))
            .limit(5)
            .collect(Collectors.toList());

        stats.put("upcoming", upcoming.stream().map(this::bookingSummary).toList());

        List<Booking> recent = bookings.stream()
            .sorted(Comparator.comparing(Booking::getBookingDate).reversed())
            .limit(5)
            .collect(Collectors.toList());

        stats.put("recent", recent.stream().map(this::bookingSummary).toList());

        return stats;
    }
}

