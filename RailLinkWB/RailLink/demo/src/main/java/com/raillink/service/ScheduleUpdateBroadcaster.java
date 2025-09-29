package com.raillink.service;

import com.raillink.model.Schedule;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ScheduleUpdateBroadcaster {

    private final List<SseEmitter> activeEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        activeEmitters.add(emitter);
        emitter.onCompletion(() -> activeEmitters.remove(emitter));
        emitter.onTimeout(() -> activeEmitters.remove(emitter));
        emitter.onError((ex) -> activeEmitters.remove(emitter));
        return emitter;
    }

    public void broadcastScheduleUpdate(Schedule schedule) {
        Map<String, Object> payload = toDto(schedule, "UPDATED");
        sendToAll(payload);
    }

    public void broadcastScheduleCreated(Schedule schedule) {
        Map<String, Object> payload = toDto(schedule, "CREATED");
        sendToAll(payload);
    }

    public void broadcastScheduleDeleted(Long scheduleId) {
        Map<String, Object> payload = Map.of(
                "type", "DELETED",
                "id", scheduleId
        );
        sendToAll(payload);
    }

    private void sendToAll(Map<String, Object> payload) {
        for (SseEmitter emitter : activeEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("schedule")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.complete();
                activeEmitters.remove(emitter);
            }
        }
    }

    private Map<String, Object> toDto(Schedule s, String type) {
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return Map.of(
                "type", type,
                "id", s.getId(),
                "status", s.getStatus(),
                "departureDate", s.getDepartureDate() != null ? iso.format(s.getDepartureDate()) : null,
                "arrivalDate", s.getArrivalDate() != null ? iso.format(s.getArrivalDate()) : null,
                "trainName", s.getTrain() != null ? s.getTrain().getName() : null,
                "routeName", s.getRoute() != null ? s.getRoute().getName() : null
        );
    }
}


