package com.raillink.service;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import com.raillink.model.Booking;
@Service
public class PdfService {
    public byte[] generateTicketPdf(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StringBuilder content = new StringBuilder();
            content.append("========================================\n");
            content.append("           RAILINK TRAIN TICKET         \n");
            content.append("========================================\n\n");
            content.append("BOOKING ID: #").append(booking.getId()).append("\n");
            content.append("PASSENGER: ").append(booking.getUser().getUsername()).append("\n");
            content.append("SEAT NUMBER: ").append(booking.getSeatNumber()).append("\n");
            content.append("TRAIN: ").append(booking.getSchedule().getTrain().getName()).append("\n");
            content.append("ROUTE: ").append(booking.getSchedule().getRoute().getName()).append("\n");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            content.append("DEPARTURE: ").append(booking.getSchedule().getDepartureDate().format(dateFormatter))
                   .append(" at ").append(booking.getSchedule().getDepartureDate().format(timeFormatter)).append("\n");
            content.append("ARRIVAL: ").append(booking.getSchedule().getArrivalDate().format(dateFormatter))
                   .append(" at ").append(booking.getSchedule().getArrivalDate().format(timeFormatter)).append("\n");
            content.append("FARE: $").append(booking.getFare() != null ? booking.getFare() : "0.00").append("\n");
            content.append("STATUS: ").append(booking.getStatus()).append("\n");
            content.append("BOOKING DATE: ").append(booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("\n\n");
            content.append("========================================\n");
            content.append("IMPORTANT INFORMATION:\n");
            content.append("• Please arrive 15 minutes before departure\n");
            content.append("• Keep this ticket for reference\n");
            content.append("• Valid for this journey only\n");
            content.append("• Generated on: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("\n");
            content.append("========================================\n");
            return content.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error generating ticket", e);
        }
    }
} 