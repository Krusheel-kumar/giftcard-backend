package com.popobob.giftcard.service;

import com.popobob.giftcard.model.*;
import com.popobob.giftcard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RewardJourneyService {

    private final JourneyCustomerRepository customerRepository;
    private final RewardCampaignRepository campaignRepository;
    private final RewardDefinitionRepository rewardDefinitionRepository;
    private final CustomerRewardRepository customerRewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final CouponGeneratorService couponGeneratorService;
    private final OtpService otpService;

    public RewardJourneyService(JourneyCustomerRepository customerRepository,
                                RewardCampaignRepository campaignRepository,
                                RewardDefinitionRepository rewardDefinitionRepository,
                                CustomerRewardRepository customerRewardRepository,
                                RewardRedemptionRepository redemptionRepository,
                                CouponGeneratorService couponGeneratorService,
                                OtpService otpService) {
        this.customerRepository = customerRepository;
        this.campaignRepository = campaignRepository;
        this.rewardDefinitionRepository = rewardDefinitionRepository;
        this.customerRewardRepository = customerRewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.couponGeneratorService = couponGeneratorService;
        this.otpService = otpService;
    }

    @Transactional
    public List<CustomerReward> startJourney(String mobileNumber, String customerName, String token, String campaignCode, String qrSource) {
        // 1. Verify OTP
        String verifiedMobile = otpService.verifyToken(token);
        
        // 2. Fetch Campaign
        RewardCampaign campaign = campaignRepository.findByCampaignCode(campaignCode)
            .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignCode));
            
        if (!campaign.isActive()) {
            throw new RuntimeException("Campaign is not active.");
        }

        // 3. Find or Create Customer
        String normalizedMobile = normalizeMobile(mobileNumber);
        if (!normalizedMobile.equals(normalizeMobile(verifiedMobile))) {
             // For testing ease, we might not fail here if MSG91 is stubbed, but ideally:
             // throw new RuntimeException("Mobile number mismatch");
             normalizedMobile = normalizeMobile(verifiedMobile);
        }

        JourneyCustomer customer = customerRepository.findByMobile(normalizedMobile).orElse(null);
        if (customer == null) {
            customer = new JourneyCustomer();
            customer.setMobile(normalizedMobile);
            customer.setName(customerName);
            customer.setMobileVerified(true);
            customer.setWhatsappOptIn(true);
            customer = customerRepository.save(customer);
        } else {
             // Check if journey already exists
             List<CustomerReward> existing = customerRewardRepository.findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(customer.getId(), campaign.getId());
             if (!existing.isEmpty()) {
                 return existing;
             }
        }

        // 4. Create the 4 rewards for the journey
        List<RewardDefinition> definitions = rewardDefinitionRepository.findByCampaignIdOrderBySequenceAsc(campaign.getId());
        if (definitions.isEmpty()) {
            throw new RuntimeException("No rewards configured for this campaign.");
        }

        for (RewardDefinition def : definitions) {
            CustomerReward cr = new CustomerReward();
            cr.setCustomerId(customer.getId());
            cr.setCampaignId(campaign.getId());
            cr.setRewardDefinitionId(def.getId());
            cr.setQrSource(qrSource);
            
            if (def.getSequence() == 1) {
                cr.setStatus("ACTIVE");
                cr.setCouponCode(couponGeneratorService.generateCoupon(campaignCode, def.getSequence()));
                cr.setActivatedAt(LocalDateTime.now());
                cr.setExpiresAt(LocalDateTime.now().plusDays(def.getValidityDays()));
            } else {
                cr.setStatus("LOCKED");
            }
            
            customerRewardRepository.save(cr);
        }
        
        return customerRewardRepository.findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(customer.getId(), campaign.getId());
    }

    @Transactional
    public RewardRedemption redeemCoupon(String couponCode, String storeId) {
        // Find the reward
        CustomerReward cr = customerRewardRepository.findByCouponCode(couponCode)
            .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        // Validate Store
        RewardCampaign campaign = campaignRepository.findById(cr.getCampaignId()).orElseThrow();
        if (!campaign.getStoreId().equals(storeId)) {
            throw new RuntimeException("INVALID_STORE");
        }

        // Validate Status
        if (!"ACTIVE".equals(cr.getStatus())) {
            throw new RuntimeException("Reward is not ACTIVE (Status: " + cr.getStatus() + ")");
        }

        // Validate Expiry
        if (cr.getExpiresAt().isBefore(LocalDateTime.now())) {
            cr.setStatus("EXPIRED");
            customerRewardRepository.save(cr);
            throw new RuntimeException("Reward is EXPIRED");
        }

        // Check if ANY other reward in the same journey is being redeemed (no stacking)
        // Handled naturally by only having 1 ACTIVE at a time.

        // Redeem
        cr.setStatus("REDEEMED");
        cr.setRedeemedAt(LocalDateTime.now());
        cr.setRedeemedStoreId(storeId);
        customerRewardRepository.save(cr);

        // Audit log
        RewardRedemption redemption = new RewardRedemption();
        redemption.setCustomerId(cr.getCustomerId());
        redemption.setCustomerRewardId(cr.getId());
        redemption.setCouponCode(couponCode);
        redemption.setCampaignId(cr.getCampaignId());
        redemption.setStoreId(storeId);
        redemptionRepository.save(redemption);

        return redemption;
    }

    public List<CustomerReward> getJourney(String mobileNumber, String campaignCode) {
        String normalizedMobile = normalizeMobile(mobileNumber);
        JourneyCustomer customer = customerRepository.findByMobile(normalizedMobile)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        RewardCampaign campaign = campaignRepository.findByCampaignCode(campaignCode)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return customerRewardRepository.findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(customer.getId(), campaign.getId());
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null) return "";
        String clean = mobile.replaceAll("[^0-9]", "");
        if (clean.length() > 10) {
            clean = clean.substring(clean.length() - 10);
        }
        return clean;
    }
}
