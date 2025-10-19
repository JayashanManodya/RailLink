package com.raillink.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.raillink.model.Route;
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByName(String name);
    Optional<Route> findByRouteCode(String routeCode);
} 