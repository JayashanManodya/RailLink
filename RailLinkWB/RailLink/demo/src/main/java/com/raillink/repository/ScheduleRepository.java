package com.raillink.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raillink.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByRoute_NameIgnoreCaseAndDepartureDateBetween(
            String routeName,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Schedule> findByRoute_IdAndDepartureDateBetween(
            Long routeId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Schedule> findByRoute_Id(Long routeId);

    List<Schedule> findByRoute_IdAndTrain_IdAndDepartureDateBetween(
            Long routeId,
            Long trainId,
            LocalDateTime start,
            LocalDateTime end
    );
}