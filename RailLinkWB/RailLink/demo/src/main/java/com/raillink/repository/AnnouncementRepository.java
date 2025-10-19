package com.raillink.repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.raillink.model.Announcement;
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByStartDateBeforeAndEndDateAfter(LocalDateTime now1, LocalDateTime now2);
    List<Announcement> findByStartDateBeforeAndEndDateAfterOrderByStartDateDesc(LocalDateTime now1, LocalDateTime now2);
    List<Announcement> findAllByOrderByStartDateDesc();
}
