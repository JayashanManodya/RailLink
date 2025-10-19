package com.raillink.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.raillink.model.Train;
@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByName(String name);
} 