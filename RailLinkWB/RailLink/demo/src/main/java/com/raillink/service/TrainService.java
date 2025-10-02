package com.raillink.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raillink.model.Train;
import com.raillink.repository.TrainRepository;

@Service
public class TrainService {

    @Autowired
    private TrainRepository trainRepository;

    public List<Train> findAllTrains() {
        return trainRepository.findAll();
    }

    public Optional<Train> findTrainById(Long id) {
        return trainRepository.findById(id);
    }

    public Train saveTrain(Train train) {
        return trainRepository.save(train);
    }

    public Train createTrain(String name, Integer capacity, String status) {
        return trainRepository
                .findByName(name)
                .orElseGet(() -> trainRepository.save(new Train(name, capacity, status)));
    }

    public Train updateTrain(Long id, Train trainDetails) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));

        train.setName(trainDetails.getName());
        train.setCapacity(trainDetails.getCapacity());
        train.setStatus(trainDetails.getStatus());

        return trainRepository.save(train);
    }

    public void deleteTrain(Long id) {
        trainRepository.deleteById(id);
    }
} 