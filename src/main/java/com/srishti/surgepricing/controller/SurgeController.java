package com.srishti.surgepricing.controller;

import com.srishti.surgepricing.model.Event;
import com.srishti.surgepricing.service.SurgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class SurgeController {

    @Autowired
    private SurgeService surgeService;

    @PostMapping("/event")
    public String addEvent(@RequestBody Event event) {
        surgeService.addEvent(event);
        return "Event added successfully!";
    }

    @GetMapping("/surge/{zone}")
    public double getSurge(@PathVariable String zone) {
        return surgeService.getSurgeMultiplier(zone);
    }
}