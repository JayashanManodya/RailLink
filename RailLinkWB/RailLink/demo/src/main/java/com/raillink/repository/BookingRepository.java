package com.raillink.repository;
import com.raillink.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByScheduleId(Long scheduleId);
    
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.train t " +
           "LEFT JOIN FETCH s.route r " +
           "WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.train t " +
           "LEFT JOIN FETCH s.route r " +
           "LEFT JOIN FETCH b.user u " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :passenger, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :passenger, '%'))")
    List<Booking> findByPassengerNameOrEmail(@Param("passenger") String passenger);
} 