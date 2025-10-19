package com.raillink.config;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import com.raillink.model.Announcement;
import com.raillink.service.AnnouncementService;
@ControllerAdvice
public class GlobalModelAttributes {
    @Autowired
    private AnnouncementService announcementService;
    @ModelAttribute
    public void addGlobalAnnouncements(Model model) {
        List<Announcement> active;
        try {
            active = announcementService.findActiveAnnouncements();
        } catch (Exception e) {
            active = Collections.emptyList();
        }
        model.addAttribute("allAnnouncements", active);
        model.addAttribute("activeAnnouncements", active);
    }
}
