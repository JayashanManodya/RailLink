package com.raillink.service;
package com.raillink.service;

// List, Optional class import panrom (collection use pannrathukku)
import java.util.List;
import java.util.Optional;

// Spring framework la irundhu autowiring and service annotation import panrom
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Namoda model (Train) and repository import panrom
import com.raillink.model.Train;
import com.raillink.repository.TrainRepository;

// @Service nu sollurathu indha class oru service layer class nu indicate pannum
@Service
public class TrainService {

    // @Autowired use pannurathu TrainRepository object automatic inject aagum
    @Autowired
    private TrainRepository trainRepository;

    // Ella train records ah database la irundhu fetch panna use pannum
    public List<Train> findAllTrains() {
        return trainRepository.findAll();
    }

    // Oru specific train ID vachu search panna use pannum
    public Optional<Train> findTrainById(Long id) {
        return trainRepository.findById(id);
    }

    // Puthiya train data save panna use pannum
    public Train saveTrain(Train train) {
        return trainRepository.save(train);
    }

    // Train name already iruka nu check pannum, illa na create pannum
    public Train createTrain(String name, Integer capacity, String status) {
        return trainRepository
                .findByName(name)
                // orElseGet means — name illa na new train create panni save pannum
                .orElseGet(() -> trainRepository.save(new Train(name, capacity, status)));
    }

    // Existing train details update panna use pannum
    public Train updateTrain(Long id, Train trainDetails) {
        // ID vachu train find pannum, illa na exception throw pannum
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        // Existing train details ah pudhu details vachu replace pannum
        train.setName(trainDetails.getName());
        train.setCapacity(trainDetails.getCapacity());
        train.setStatus(trainDetails.getStatus());

        // Updated train record save pannum
        return trainRepository.save(train);
    }

    // Train record delete panna ID vachu delete pannum
    public void deleteTrain(Long id) {
        trainRepository.deleteById(id);
    }
}
