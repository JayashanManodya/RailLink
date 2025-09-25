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

    @Autowired
    private ScheduleUpdateBroadcaster broadcaster;

    @CrossOrigin
    @GetMapping(path = "/schedules", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSchedules() {
        return broadcaster.subscribe();
    }
}


