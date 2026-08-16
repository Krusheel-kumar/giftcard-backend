package com.popobob.giftcard.controller;

import com.popobob.giftcard.service.BogoService;
import com.popobob.giftcard.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bogo")
@CrossOrigin(origins = "*")
public class BogoController {

    @Autowired
    private BogoService bogoService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @PostMapping("/claim")
    public ResponseEntity<?> claim(@RequestBody Map<String, String> body) {
        try {
            String mobile = body.get("mobileNumber");
            if (mobile == null) throw new RuntimeException("Mobile number is required");
            Bucket bucket = rateLimitingService.resolveBucket(mobile);
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429).body(Map.of("message", "Too many requests. Please try again later."));
            }
            bogoService.claim(mobile);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        try {
            String mobile = body.get("mobileNumber");
            if (mobile == null) throw new RuntimeException("Mobile number is required");
            Bucket bucket = rateLimitingService.resolveBucket(mobile);
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429).body(Map.of("message", "Too many requests. Please try again later."));
            }
            return ResponseEntity.ok(bogoService.verify(mobile, body.get("customerName"), body.get("token")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> body) {
        try {
            String code = (String) body.get("code");
            String storeId = (String) body.get("storeId");
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("cartItems");
            return ResponseEntity.ok(bogoService.validate(code, storeId, cartItems));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestBody Map<String, String> body) {
        try {
            bogoService.redeem(body.get("code"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/lookup")
    public ResponseEntity<?> adminLookup(@RequestBody Map<String, String> body) {
        try {
            var code = bogoService.lookupAdmin(body.get("code"));
            return ResponseEntity.ok(Map.of(
                "valid", "ACTIVE".equals(code.getStatus()),
                "status", code.getStatus(),
                "mobileNumber", code.getMobileNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            return ResponseEntity.ok(bogoService.getAdminStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
