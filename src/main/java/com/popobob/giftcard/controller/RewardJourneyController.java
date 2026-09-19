package com.popobob.giftcard.controller;

import com.popobob.giftcard.model.CustomerReward;
import com.popobob.giftcard.model.RewardRedemption;
import com.popobob.giftcard.service.RewardJourneyService;
import com.popobob.giftcard.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "*")
public class RewardJourneyController {

    private final RewardJourneyService rewardJourneyService;
    private final RateLimitingService rateLimitingService;
    private final com.popobob.giftcard.config.JwtUtil jwtUtil;

    public RewardJourneyController(RewardJourneyService rewardJourneyService, RateLimitingService rateLimitingService, com.popobob.giftcard.config.JwtUtil jwtUtil) {
        this.rewardJourneyService = rewardJourneyService;
        this.rateLimitingService = rateLimitingService;
        this.jwtUtil = jwtUtil;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    @PostMapping("/campaign/{campaignCode}/verify")
    public ResponseEntity<?> verifyAndStartJourney(@PathVariable String campaignCode, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String ip = getClientIp(httpRequest);
            Bucket bucket = rateLimitingService.resolveIpBucket(ip);
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("success", false, "message", "Too many requests. Please try again later."));
            }
            String mobileNumber = request.get("mobileNumber");
            String customerName = request.get("customerName");
            String token = request.get("token");
            String qrSource = request.getOrDefault("source", "UNKNOWN");
            
            List<CustomerReward> journey = rewardJourneyService.startJourney(mobileNumber, customerName, token, campaignCode, qrSource);
            
            // Generate a 6-month token for seamless re-entry
            long sixMonthsInMillis = 1000L * 60 * 60 * 24 * 180;
            String jwt = jwtUtil.generateToken(mobileNumber, "CUSTOMER", sixMonthsInMillis);
            
            return ResponseEntity.ok(Map.of(
                "journey", journey,
                "token", jwt
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCoupon(@RequestBody Map<String, String> request) {
        try {
            String couponCode = request.get("couponCode");
            String storeId = request.get("storeId"); // e.g. FILM_NAGAR
            RewardRedemption redemption = rewardJourneyService.redeemCoupon(couponCode, storeId);
            return ResponseEntity.ok(redemption);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/me/{campaignCode}")
    public ResponseEntity<?> getMyJourney(@PathVariable String campaignCode, java.security.Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
            }
            List<CustomerReward> journey = rewardJourneyService.getJourney(principal.getName(), campaignCode);
            return ResponseEntity.ok(journey);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
