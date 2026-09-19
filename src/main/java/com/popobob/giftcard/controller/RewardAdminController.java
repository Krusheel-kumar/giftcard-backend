package com.popobob.giftcard.controller;

import com.popobob.giftcard.model.CustomerReward;
import com.popobob.giftcard.model.JourneyCustomer;
import com.popobob.giftcard.model.RewardDefinition;
import com.popobob.giftcard.repository.CustomerRewardRepository;
import com.popobob.giftcard.repository.JourneyCustomerRepository;
import com.popobob.giftcard.repository.RewardDefinitionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards/admin")
@CrossOrigin(origins = "*")
public class RewardAdminController {

    private final CustomerRewardRepository customerRewardRepository;
    private final JourneyCustomerRepository customerRepository;
    private final RewardDefinitionRepository rewardDefinitionRepository;

    public RewardAdminController(CustomerRewardRepository customerRewardRepository,
                                 JourneyCustomerRepository customerRepository,
                                 RewardDefinitionRepository rewardDefinitionRepository) {
        this.customerRewardRepository = customerRewardRepository;
        this.customerRepository = customerRepository;
        this.rewardDefinitionRepository = rewardDefinitionRepository;
    }

    @PostMapping("/lookup")
    public ResponseEntity<?> lookupReward(@RequestBody Map<String, String> request) {
        try {
            String couponCode = request.get("code");
            if (couponCode == null || couponCode.trim().isEmpty()) {
                throw new RuntimeException("Code is required");
            }

            CustomerReward cr = customerRewardRepository.findByCouponCode(couponCode)
                    .orElseThrow(() -> new RuntimeException("Invalid or unrecognized code"));

            JourneyCustomer customer = customerRepository.findById(cr.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            RewardDefinition def = rewardDefinitionRepository.findById(cr.getRewardDefinitionId())
                    .orElseThrow(() -> new RuntimeException("Reward definition not found"));

            List<CustomerReward> journeyList = customerRewardRepository.findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(cr.getCustomerId(), cr.getCampaignId());
            List<Map<String, Object>> mappedJourney = journeyList.stream().map(j -> {
                RewardDefinition jDef = rewardDefinitionRepository.findById(j.getRewardDefinitionId()).orElseThrow();
                Map<String, Object> map = new HashMap<>();
                map.put("sequence", jDef.getSequence());
                map.put("name", jDef.getName());
                map.put("status", j.getStatus());
                map.put("redeemedAt", j.getRedeemedAt());
                return map;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("valid", "ACTIVE".equals(cr.getStatus()));
            response.put("status", cr.getStatus());
            response.put("mobileNumber", customer.getMobile());
            response.put("customerName", customer.getName());
            response.put("rewardName", def.getName());
            response.put("rewardDescription", def.getDescription());
            response.put("expiresAt", cr.getExpiresAt());
            response.put("journey", mappedJourney);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            long totalGenerated = customerRewardRepository.count();
            long totalRedeemed = customerRewardRepository.countByStatus("REDEEMED");

            // Fetch a few recent records for the dashboard
            List<Map<String, Object>> records = customerRewardRepository.findTop50ByOrderByIdDesc().stream()
                    .map(cr -> {
                        JourneyCustomer cust = customerRepository.findById(cr.getCustomerId()).orElse(new JourneyCustomer());
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", cust.getName() != null ? cust.getName() : "Unknown");
                        map.put("mobileNumber", cust.getMobile());
                        map.put("code", cr.getCouponCode());
                        map.put("status", cr.getStatus());
                        map.put("generatedAt", cr.getActivatedAt() != null ? cr.getActivatedAt().toString() : null);
                        map.put("redeemedAt", cr.getRedeemedAt() != null ? cr.getRedeemedAt().toString() : null);
                        return map;
                    }).toList();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGenerated", totalGenerated);
            stats.put("totalRedeemed", totalRedeemed);
            stats.put("records", records);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
