package com.raillink.repository; // package name (repository package la iruku - DB access files)

import java.util.Optional; // null value avoid panna optional use aagum
import org.springframework.data.jpa.repository.JpaRepository; // Spring JPA repository import
import org.springframework.stereotype.Repository; // Repository annotation use panna
import com.raillink.model.Route; // Route model import pannirukom

@Repository // indha interface database repository nu indicate pannudhu
public interface RouteRepository extends JpaRepository<Route, Long> {
    // JpaRepository -> basic CRUD (Create, Read, Update, Delete) methods ready-a irukum

    Optional<Route> findByName(String name); 
    // name base la route search panna use pannum method

    Optional<Route> findByRouteCode(String routeCode); 
    // routeCode base la route search panna use pannum method..
}
