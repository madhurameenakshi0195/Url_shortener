package com.urlshortener.controller;

import com.urlshortener.service.RedisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redis")
public class RedisTestController {

    private final RedisService redisService;

    public RedisTestController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/test")
    public String saveTestValue() {
        redisService.save("test-key", "Hello Redis!");
        return "Saved to Redis";
    }

    @GetMapping("/test")
    public String getTestValue() {
        String value = redisService.get("test-key");

        if (value == null) {
            return "Value not found";
        }

        return value;
    }
}