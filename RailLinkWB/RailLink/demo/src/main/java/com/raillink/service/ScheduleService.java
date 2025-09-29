package com.raillink.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raillink.model.Route;
import com.raillink.model.Schedule;
import com.raillink.model.Train;
import com.raillink.repository.ScheduleRepository;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public List<Schedule> findAllSchedules() {
        return scheduleRepository.findAll();
    }

    /**
     * Filter schedules by a simplistic route name built from fromStation + " to " + toStation
     * and by calendar date (inclusive day range).
     */
    public List<Schedule> searchByStationsAndDate(String fromStation, String toStation, String dateIso) {
        if (fromStation == null || toStation == null || dateIso == null ||
            fromStation.isBlank() || toStation.isBlank() || dateIso.isBlank()) {
            return java.util.Collections.emptyList();
        }

        LocalDateTime startOfDay = LocalDateTime.parse(dateIso + "T00:00:00");
        LocalDateTime endOfDay = LocalDateTime.parse(dateIso + "T23:59:59");

        // First try an exact route name match (legacy behavior)
        String exactRouteName = (fromStation + " to " + toStation).trim();
        List<Schedule> exactMatches = scheduleRepository
                .findByRoute_NameIgnoreCaseAndDepartureDateBetween(exactRouteName, startOfDay, endOfDay);
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }

        // Fallback: fuzzy match where route name contains both station tokens (order-insensitive), ignoring punctuation/case
        final String tokenFrom = normalizeToken(fromStation);
        final String tokenTo = normalizeToken(toStation);
        return scheduleRepository.findAll().stream()
                .filter(s -> {
                    LocalDateTime dep = s.getDepartureDate();
                    return (dep.isAfter(startOfDay) || dep.isEqual(startOfDay))
                            && (dep.isBefore(endOfDay) || dep.isEqual(endOfDay));
                })
                .filter(s -> {
                    String name = s.getRoute() != null ? s.getRoute().getName() : "";
                    String normalized = normalizeToken(name);
                    return normalized.contains(tokenFrom) && normalized.contains(tokenTo);
                })
                .toList();
    }

    private String normalizeToken(String value) {
        if (value == null) return "";
        String v = value.toLowerCase();
        // replace separators with space and strip non-alphanumerics
        v = v.replaceAll("to", " ");
        v = v.replaceAll("[^a-z0-9]+", " ").trim();
        return v;
    }

    /**
     * Filter schedules by route id and date (calendar day).
     */
    public List<Schedule> searchByRouteAndDate(Long routeId, String dateIso) {
        if (routeId == null || dateIso == null || dateIso.isBlank()) {
            return scheduleRepository.findAll();
        }
        LocalDateTime startOfDay = LocalDateTime.parse(dateIso + "T00:00:00");
        LocalDateTime endOfDay = LocalDateTime.parse(dateIso + "T23:59:59");
        List<Schedule> daySchedules = scheduleRepository.findByRoute_IdAndDepartureDateBetween(routeId, startOfDay, endOfDay);
        if (!daySchedules.isEmpty()) {
            return daySchedules;
        }

        // If no schedules exist for the date, auto-generate daily instances based on existing templates for this route
        List<Schedule> templates = scheduleRepository.findByRoute_Id(routeId);
        if (templates.isEmpty()) {
            return daySchedules;
        }

        // For each template, create schedule on the selected date if not already present
        for (Schedule tmpl : templates) {
            LocalDateTime dep = tmpl.getDepartureDate();
            LocalDateTime arr = tmpl.getArrivalDate();
            LocalDateTime newDep = startOfDay.withHour(dep.getHour()).withMinute(dep.getMinute()).withSecond(dep.getSecond());
            LocalDateTime newArr = startOfDay.withHour(arr.getHour()).withMinute(arr.getMinute()).withSecond(arr.getSecond());

            boolean exists = !scheduleRepository
                    .findByRoute_IdAndTrain_IdAndDepartureDateBetween(routeId, tmpl.getTrain().getId(), startOfDay, endOfDay)
                    .isEmpty();
            if (!exists) {
                Schedule s = new Schedule(newDep, newArr, tmpl.getStatus(), tmpl.getTrain(), tmpl.getRoute());
                scheduleRepository.save(s);
            }
        }

        return scheduleRepository.findByRoute_IdAndDepartureDateBetween(routeId, startOfDay, endOfDay);
    }

    public Optional<Schedule> findScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule createSchedule(LocalDateTime departureDate, LocalDateTime arrivalDate, 
                                 String status, Train train, Route route) {
        Schedule schedule = new Schedule(departureDate, arrivalDate, status, train, route);
        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Long id, Schedule scheduleDetails) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setDepartureDate(scheduleDetails.getDepartureDate());
        schedule.setArrivalDate(scheduleDetails.getArrivalDate());
        schedule.setStatus(scheduleDetails.getStatus());
        schedule.setTrain(scheduleDetails.getTrain());
        schedule.setRoute(scheduleDetails.getRoute());

        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    public List<Schedule> findSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // This would need a custom query in the repository
        // For now, returning all schedules
        return scheduleRepository.findAll();
    }
} 