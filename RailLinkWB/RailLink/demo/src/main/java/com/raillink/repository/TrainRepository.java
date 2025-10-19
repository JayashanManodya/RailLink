package com.raillink.repository;

// Optional class import panrom (return value null irundha safe handle panna)
import java.util.Optional;

// JpaRepository import pannurathu database operations easy aaka use pannurathu
import org.springframework.data.jpa.repository.JpaRepository;

// @Repository annotation use pannurathu — indha interface oru repository class nu sollum
import org.springframework.stereotype.Repository;

// Namoda model class (Train) import panrom
import com.raillink.model.Train;

// @Repository use pannurathu — Spring ku idhu database layer nu identify panna help pannum
@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    // findByName method — train name vachu search panna use pannum
    // Optional return pannum — name match aagala na null handle safe aagum
    Optional<Train> findByName(String name);
}
