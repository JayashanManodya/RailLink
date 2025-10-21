package com.raillink.repository;
import com.raillink.model.ScheduleStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ScheduleStationRepository extends JpaRepository<ScheduleStation, Long> {
    List<ScheduleStation> findByScheduleIdOrderByStationOrder(Long scheduleId);
    List<ScheduleStation> findByStationId(Long stationId);
    void deleteByScheduleId(Long scheduleId);
}
