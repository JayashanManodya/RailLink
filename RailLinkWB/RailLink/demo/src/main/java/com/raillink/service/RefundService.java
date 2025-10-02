package com.raillink.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raillink.model.Booking;
import com.raillink.model.Refund;
import com.raillink.repository.RefundRepository;

@Service
public class RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private BookingService bookingService;

    public Refund requestRefund(Long bookingId, BigDecimal amount, String reason) {
        Booking booking = bookingService.findBookingById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Refund refund = new Refund(booking, amount, reason, "REQUESTED");
        return refundRepository.save(refund);
    }

    public Refund approveRefund(Long refundId, String adminUsername) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        refund.setStatus("APPROVED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(adminUsername);
        return refundRepository.save(refund);
    }

    public Refund rejectRefund(Long refundId, String adminUsername) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        refund.setStatus("REJECTED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(adminUsername);
        return refundRepository.save(refund);
    }

    public Refund markIssued(Long refundId, String adminUsername) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));
        refund.setStatus("ISSUED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(adminUsername);
        return refundRepository.save(refund);
    }

    public List<Refund> findRefundsByBooking(Long bookingId) {
        return refundRepository.findByBookingId(bookingId);
    }

    public List<Refund> findAll() {
        return refundRepository.findAll();
    }
}


