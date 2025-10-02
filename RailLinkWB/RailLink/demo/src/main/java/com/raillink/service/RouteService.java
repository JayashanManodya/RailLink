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

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route createRoute(String name, String description) {
        return routeRepository
                .findByName(name)
                .orElseGet(() -> routeRepository.save(new Route(name, description)));
    }

    public Route updateRoute(Long id, Route routeDetails) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setName(routeDetails.getName());
        route.setDescription(routeDetails.getDescription());
        route.setPath(routeDetails.getPath());

        return routeRepository.save(route);
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
} 