package com.raillink.service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.raillink.model.Route;
import com.raillink.repository.RouteRepository;
@Service
public class RouteService {
    @Autowired
    private RouteRepository routeRepository;
    public List<Route> findAllRoutes() {
        return routeRepository.findAll();
    }
    public Optional<Route> findRouteById(Long id) {
        return routeRepository.findById(id);
    }
    public Optional<Route> findRouteByCode(String routeCode) {
        return routeRepository.findByRouteCode(routeCode);
    }
    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }
    public Route createRoute(String name, String description, String routeCode, String path) {
        return routeRepository
                .findByName(name)
                .orElseGet(() -> routeRepository.save(new Route(name, description, routeCode, path)));
    }
    public Route updateRoute(Long id, Route routeDetails) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        route.setName(routeDetails.getName());
        route.setDescription(routeDetails.getDescription());
        route.setRouteCode(routeDetails.getRouteCode());
        route.setPath(routeDetails.getPath());
        return routeRepository.save(route);
    }
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
    public String convertStationIdsToPath(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return "";
        }
        return stationIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
    public List<Long> convertPathToStationIds(String path) {
        if (path == null || path.trim().isEmpty()) {
            return List.of();
        }
        return List.of(path.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
} 