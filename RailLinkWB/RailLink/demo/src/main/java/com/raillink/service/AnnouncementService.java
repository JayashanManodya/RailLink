package com.raillink.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.raillink.model.Announcement;
import com.raillink.repository.AnnouncementRepository;
@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementRepository announcementRepository;
    public List<Announcement> findAll() { return announcementRepository.findAllByOrderByStartDateDesc(); }
    public Optional<Announcement> findById(Long id) { return announcementRepository.findById(id); }
    public Announcement save(Announcement a) {
        LocalDateTime now = LocalDateTime.now();
        if (a.getStartDate() == null) a.setStartDate(now);
        if (a.getEndDate() == null || a.getEndDate().isBefore(a.getStartDate())) a.setEndDate(a.getStartDate().plusDays(1));
        if (a.getTitle() == null || a.getTitle().isBlank()) a.setTitle("Announcement");
        return announcementRepository.save(a);
    }
    public void deleteById(Long id) { announcementRepository.deleteById(id); }
    public List<Announcement> findActiveAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        return announcementRepository.findByStartDateBeforeAndEndDateAfterOrderByStartDateDesc(now, now);
    }
}
