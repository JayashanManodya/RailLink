package com.raillink.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.raillink.model.Booking;

@Service
public class PdfService {

    public byte[] generateTicketPdf(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StringBuilder content = new StringBuilder();
            content.append("RailLink Ticket\n");
            content.append("Booking ID: ").append(booking.getId()).append("\n");
            content.append("Passenger: ").append(booking.getUser().getUsername()).append("\n");
            content.append("Seat: ").append(booking.getSeatNumber()).append("\n");
            content.append("Train: ").append(booking.getSchedule().getTrain().getName()).append("\n");
            content.append("Route: ").append(booking.getSchedule().getRoute().getName()).append("\n");
            content.append("Departure: ").append(booking.getSchedule().getDepartureDate()).append("\n");
            content.append("Arrival: ").append(booking.getSchedule().getArrivalDate()).append("\n");
            content.append("Status: ").append(booking.getStatus()).append("\n");
            return content.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error generating ticket", e);
        }
    }
} 