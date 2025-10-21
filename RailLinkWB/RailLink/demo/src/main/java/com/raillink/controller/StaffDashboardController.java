package com.raillink.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.raillink.service.AnnouncementService;
@Controller
public class StaffDashboardController {
    @Autowired
    private AnnouncementService announcementService;
    @GetMapping("/staff/dashboard")
    public String staffDashboard(Model model) {
        java.util.List<com.raillink.model.Announcement> activeAnnouncements = announcementService.findActiveAnnouncements();
        System.out.println("DEBUG: Staff Dashboard - Found " + activeAnnouncements.size() + " active announcements");
        for (com.raillink.model.Announcement a : activeAnnouncements) {
            System.out.println("DEBUG: Staff Announcement - " + a.getTitle() + " (Start: " + a.getStartDate() + ", End: " + a.getEndDate() + ")");
        }
        model.addAttribute("allAnnouncements", activeAnnouncements);
        return "staff/dashboard";
    }
    @GetMapping("/staff/debug/create-announcement")
    public String createTestAnnouncementForStaff() {
        try {
            com.raillink.model.Announcement testAnnouncement = new com.raillink.model.Announcement();
            testAnnouncement.setTitle("Staff Test Announcement");
            testAnnouncement.setMessage("This is a test announcement specifically for staff members to verify the system is working.");
            testAnnouncement.setStartDate(java.time.LocalDateTime.now().minusHours(1));
            testAnnouncement.setEndDate(java.time.LocalDateTime.now().plusDays(7));
            testAnnouncement.setAuthor("staff-system");
            announcementService.save(testAnnouncement);
            System.out.println("DEBUG: Staff test announcement created successfully");
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating staff test announcement: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }
}
