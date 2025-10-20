package com.raillink.controller;

import com.raillink.service.ScheduleUpdateBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class ScheduleSseController {

    // Schedule update ah broadcast panna use aagura service ah inject pannrom
    @Autowired
    private ScheduleUpdateBroadcaster broadcaster;

    // Cross-origin allow pannrom, appo vera domain frontend irundhaalum connect aagum
    @CrossOrigin

    // "/api/sse/schedules" endpoint ku GET request vandha,
    // real-time schedule updates stream pannum (SSE)
    // MediaType.TEXT_EVENT_STREAM_VALUE -> Server-Sent Events format
    @GetMapping(path = "/schedules", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSchedules() {
        // Client connect aana udane, broadcaster la subscribe pannum
        // appuram schedule updates live ah client ku send aagum
        return broadcaster.subscribe();
    }
}
