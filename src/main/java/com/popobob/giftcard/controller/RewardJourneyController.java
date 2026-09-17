package com.popobob.giftcard.controller;

import com.popobob.giftcard.model.CustomerReward;
import com.popobob.giftcard.model.RewardRedemption;
import com.popobob.giftcard.service.RewardJourneyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "*")
public class RewardJourneyController {

    private final RewardJourneyService rewardJourneyService;

    public RewardJourneyController(RewardJourneyService rewardJourneyService) {
        this.rewardJourneyService = rewardJourneyService;
    }

    @PostMapping("/campaign/{campaignCode}/verify")
    public ResponseEntity<?> verifyAndStartJourney(@PathVariable String campaignCode, @RequestBody Map<String, String> request) {
        try {
            String mobileNumber = request.get("mobileNumber");
            String customerName = request.get("customerName");
            String token = request.get("token");
            String qrSource = request.getOrDefault("source", "UNKNOWN");
            
            List<CustomerReward> journey = rewardJourneyService.startJourney(mobileNumber, customerName, token, campaignCode, qrSource);
            return ResponseEntity.ok(journey);
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

    @GetMapping("/customer/{mobile}/campaign/{campaignCode}")
    public ResponseEntity<?> getCustomerJourney(@PathVariable String mobile, @PathVariable String campaignCode) {
        try {
            List<CustomerReward> journey = rewardJourneyService.getJourney(mobile, campaignCode);
            return ResponseEntity.ok(journey);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
