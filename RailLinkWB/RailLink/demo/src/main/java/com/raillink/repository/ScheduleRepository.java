package com.raillink.repository; // Package name where this repository class is located

import java.time.LocalDateTime; // Importing LocalDateTime for date and time handling
import java.util.List; // Importing List to return multiple Schedule records
import org.springframework.data.jpa.repository.JpaRepository; // Importing JpaRepository for database operations
import org.springframework.stereotype.Repository; // Marks this interface as a Spring Repository
import com.raillink.model.Schedule; // Importing the Schedule model class

@Repository // This annotation tells Spring that this interface is a repository (used to interact with DB)
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // This interface extends JpaRepository which provides built-in CRUD (Create, Read, Update, Delete) operations
    // JpaRepository<Schedule, Long> means it handles Schedule entities with primary key of type Long

    // Find schedules by route name (case-insensitive) and departure date between two given times
    List<Schedule> findByRoute_NameIgnoreCaseAndDepartureDateBetween(
            String routeName,          // Route name to match (case ignored)
            LocalDateTime start,       // Start time for filtering departure date
            LocalDateTime end          // End time for filtering departure date
    );

    // Find schedules by route ID and departure date between two given times
    List<Schedule> findByRoute_IdAndDepartureDateBetween(
            Long routeId,              // Route ID to filter schedules
            LocalDateTime start,       // Start date/time
            LocalDateTime end          // End date/time
    );

    // Find all schedules that belong to a specific route using route ID
    List<Schedule> findByRoute_Id(Long routeId); // Returns all schedules for that route ID

    // Find schedules by both route ID and train ID within a specific departure date range
    List<Schedule> findByRoute_IdAndTrain_IdAndDepartureDateBetween(
            Long routeId,              // Route ID for filtering
            Long trainId,              // Train ID for filtering
            LocalDateTime start,       // Start date/time for filtering
            LocalDateTime end          // End date/time for filtering
    );
}
