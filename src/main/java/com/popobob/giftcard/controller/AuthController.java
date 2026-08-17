package com.popobob.giftcard.controller;

import com.popobob.giftcard.config.JwtUtil;
import com.popobob.giftcard.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Value("${admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        // IP-based login rate limiting: 5 attempts per hour
        String ip = request.getRemoteAddr();
        Bucket loginBucket = rateLimitingService.resolveIpBucket("login_" + ip);
        if (!loginBucket.tryConsume(1)) {
            return ResponseEntity.status(429).body(Map.of("message", "Too many login attempts. Please try again later."));
        }

        String password = body.get("password");
        if (password == null) {
            password = body.get("pin"); // Backwards compatibility for staff-admin UI
        }
        
        if (adminPassword.equals(password)) {
            String token = jwtUtil.generateToken("admin", "ADMIN");
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials."));
        }
    }
}
