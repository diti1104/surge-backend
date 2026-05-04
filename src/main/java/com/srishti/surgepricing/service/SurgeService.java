package com.srishti.surgepricing.service;

import com.srishti.surgepricing.model.Event;
import com.srishti.surgepricing.repository.SurgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SurgeService {

    @Autowired
    private SurgeRepository surgeRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void addEvent(Event event) {
        surgeRepository.save(event);

        String zone = event.getZone();
        String riderKey = "riders:" + zone;
        String driverKey = "drivers:" + zone;

        if (event.getType().equalsIgnoreCase("RIDER")) {
            redisTemplate.opsForValue().increment(riderKey);
        } else {
            redisTemplate.opsForValue().increment(driverKey);
        }

        double surge = getSurgeMultiplier(zone);

        System.out.println("🔥 Zone: " + zone + " | Surge: " + surge);
        System.out.println("📡 Sending to /topic/surge/" + zone);

        messagingTemplate.convertAndSend("/topic/surge/" + zone, surge);

        System.out.println("✅ WebSocket message sent!");
    }

    public double getSurgeMultiplier(String zone) {
        String riderKey = "riders:" + zone;
        String driverKey = "drivers:" + zone;

        int riders = getValue(riderKey);
        int drivers = getValue(driverKey);

        double baseSurge;
        double maxSurge;

        switch (zone.toUpperCase()) {
            case "HIGH":
                baseSurge = 2.0;
                maxSurge = 5.0;
                break;
            case "MEDIUM":
                baseSurge = 1.5;
                maxSurge = 3.5;
                break;
            case "LOW":
            default:
                baseSurge = 1.0;
                maxSurge = 2.0;
                break;
        }

        if (drivers == 0 && riders == 0) return baseSurge;
        if (drivers == 0) return maxSurge;

        double ratio = (double) riders / drivers;
        double surge;

        if (ratio <= 0.5) surge = baseSurge * 0.8;
        else if (ratio <= 1.0) surge = baseSurge;
        else if (ratio <= 1.5) surge = baseSurge * 1.3;
        else if (ratio <= 2.0) surge = baseSurge * 1.6;
        else if (ratio <= 3.0) surge = baseSurge * 2.0;
        else surge = maxSurge;

        surge = Math.max(1.0, Math.min(surge, maxSurge));
        return Math.round(surge * 10.0) / 10.0;
    }

    private int getValue(String key) {
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0 : Integer.parseInt(val);
    }
}