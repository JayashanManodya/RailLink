package com.raillink.service;
import com.raillink.model.ScheduleStation;
import com.raillink.model.Station;
import com.raillink.repository.ScheduleStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class ScheduleStationService {
    @Autowired
    private ScheduleStationRepository scheduleStationRepository;
    public List<ScheduleStation> findByScheduleId(Long scheduleId) {
        return scheduleStationRepository.findByScheduleIdOrderByStationOrder(scheduleId);
    }
    public List<ScheduleStation> findByStationId(Long stationId) {
        return scheduleStationRepository.findByStationId(stationId);
    }
    public ScheduleStation saveScheduleStation(ScheduleStation scheduleStation) {
        return scheduleStationRepository.save(scheduleStation);
    }
    public void deleteByScheduleId(Long scheduleId) {
        scheduleStationRepository.deleteByScheduleId(scheduleId);
    }
    public ScheduleStation createScheduleStation(Long scheduleId, Long stationId, 
                                                LocalDateTime arrivalTime, LocalDateTime departureTime, 
                                                Integer stationOrder, String platform) {
        ScheduleStation scheduleStation = new ScheduleStation();
        scheduleStation.setArrivalTime(arrivalTime);
        scheduleStation.setDepartureTime(departureTime);
        scheduleStation.setStationOrder(stationOrder);
        scheduleStation.setPlatform(platform);
        return scheduleStationRepository.save(scheduleStation);
    }
}
