package com.raillink.service; 

import java.util.List; 
import java.util.Optional; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Service; 
import com.raillink.model.Route;
import com.raillink.repository.RouteRepository; 

@Service // indha class service layer nu indicate pannudhu
public class RouteService {

    @Autowired // RouteRepository object automatic inject aagum
    private RouteRepository routeRepository;

    public List<Route> findAllRoutes() {
        // database la iruka ella routes um fetch panna
        return routeRepository.findAll();
    }

    public Optional<Route> findRouteById(Long id) {
        // id base la single route ah find panna
        return routeRepository.findById(id);
    }

    public Optional<Route> findRouteByCode(String routeCode) {
        // routeCode base la route search panna
        return routeRepository.findByRouteCode(routeCode);
    }

    public Route saveRoute(Route route) {
        // route object save panna (new ah illa update ah irundha save aagum)
        return routeRepository.save(route);
    }

    public Route createRoute(String name, String description, String routeCode, String path) {
        // name already irundha adha use pannum
        // illa na new route create pannum
        return routeRepository
                .findByName(name)
                .orElseGet(() -> routeRepository.save(new Route(name, description, routeCode, path)));
    }

    public Route updateRoute(Long id, Route routeDetails) {
        // existing route update panna 
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found")); 

        // old route details ah new values la update pannudhu..
        route.setName(routeDetails.getName());
        route.setDescription(routeDetails.getDescription());
        route.setRouteCode(routeDetails.getRouteCode());
        route.setPath(routeDetails.getPath());

        return routeRepository.save(route); 
    }

    public void deleteRoute(Long id) {
        // id base la route delete panna
        routeRepository.deleteById(id);
    }

    public String convertStationIdsToPath(List<Long> stationIds) {
        // station IDs list ah string path (comma separated) ah convert pannudhu
        if (stationIds == null || stationIds.isEmpty()) {
            return ""; 
        }
        return stationIds.stream()
                .map(String::valueOf) 
                .reduce((a, b) -> a + "," + b)
                .orElse(""); 
    }

    public List<Long> convertPathToStationIds(String path) {
        // path string ah station ID list ah convert pannudhu
        if (path == null || path.trim().isEmpty()) {
            return List.of(); 
        }
        return List.of(path.split(",")) // comma la split pannudhu
                .stream()
                .map(String::trim) 
                .filter(s -> !s.isEmpty()) 
                .map(Long::valueOf) 
                .toList(); 
    }
}
