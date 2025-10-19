package com.raillink.service; // package name (service layer la iruku)

import java.util.List; // List use panna import
import java.util.Optional; // null avoid panna Optional use panna
import org.springframework.beans.factory.annotation.Autowired; // dependency inject panna use aagum
import org.springframework.stereotype.Service; // service class nu mark panna
import com.raillink.model.Route; // Route model import pannirukom
import com.raillink.repository.RouteRepository; // repository import pannirukom

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
        // existing route update panna use pannum
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found")); // id kidaikala na error throw pannum

        // old route details ah new values la update pannudhu
        route.setName(routeDetails.getName());
        route.setDescription(routeDetails.getDescription());
        route.setRouteCode(routeDetails.getRouteCode());
        route.setPath(routeDetails.getPath());

        return routeRepository.save(route); // updated route save pannudhu
    }

    public void deleteRoute(Long id) {
        // id base la route delete panna
        routeRepository.deleteById(id);
    }

    public String convertStationIdsToPath(List<Long> stationIds) {
        // station IDs list ah string path (comma separated) ah convert pannudhu
        if (stationIds == null || stationIds.isEmpty()) {
            return ""; // empty list na empty string return pannum
        }
        return stationIds.stream()
                .map(String::valueOf) // number ah string ah maathudhu
                .reduce((a, b) -> a + "," + b) // comma vachu join pannudhu
                .orElse(""); // result illa na empty string return pannudhu
    }

    public List<Long> convertPathToStationIds(String path) {
        // path string ah station ID list ah convert pannudhu
        if (path == null || path.trim().isEmpty()) {
            return List.of(); // empty path na empty list return pannudhu
        }
        return List.of(path.split(",")) // comma la split pannudhu
                .stream()
                .map(String::trim) // space remove pannudhu
                .filter(s -> !s.isEmpty()) // empty values filter pannudhu
                .map(Long::valueOf) // string ah number ah maathudhu
                .toList(); // list ah collect pannudhu
    }
}
