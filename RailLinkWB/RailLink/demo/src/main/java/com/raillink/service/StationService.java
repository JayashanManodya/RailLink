package com.raillink.service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.raillink.model.Station;
import com.raillink.repository.StationRepository;
@Service
public class StationService {
    @Autowired
    private StationRepository stationRepository;
    public List<Station> findAllStations() {
        return stationRepository.findAll().stream()
                .filter(station -> !station.isArchived())
                .toList();
    }
    public List<Station> findAllStationsIncludingArchived() {
        return stationRepository.findAll();
    }
    public Optional<Station> findStationById(Long id) {
        return stationRepository.findById(id);
    }
    public Station saveStation(Station station) {
        return stationRepository.save(station);
    }
    public Station createStation(String name, String location) {
        return stationRepository
                .findByName(name)
                .orElseGet(() -> stationRepository.save(new Station(name, location)));
    }
    public Station updateStation(Long id, Station stationDetails) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        station.setName(stationDetails.getName());
        station.setLocation(stationDetails.getLocation());
        station.setFacilities(stationDetails.getFacilities());
        station.setArchived(stationDetails.isArchived());
        return stationRepository.save(station);
    }
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }
    public void restoreStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        station.setArchived(false);
        stationRepository.save(station);
    }
} 