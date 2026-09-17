package com.popobob.giftcard.service;

import com.popobob.giftcard.model.CustomerReward;
import com.popobob.giftcard.model.RewardCampaign;
import com.popobob.giftcard.model.RewardDefinition;
import com.popobob.giftcard.repository.CustomerRewardRepository;
import com.popobob.giftcard.repository.RewardDefinitionRepository;
import com.popobob.giftcard.repository.RewardCampaignRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardJobService {

    private final CustomerRewardRepository customerRewardRepository;
    private final RewardDefinitionRepository rewardDefinitionRepository;
    private final RewardCampaignRepository campaignRepository;
    private final CouponGeneratorService couponGeneratorService;

    public RewardJobService(CustomerRewardRepository customerRewardRepository,
                            RewardDefinitionRepository rewardDefinitionRepository,
                            RewardCampaignRepository campaignRepository,
                            CouponGeneratorService couponGeneratorService) {
        this.customerRewardRepository = customerRewardRepository;
        this.rewardDefinitionRepository = rewardDefinitionRepository;
        this.campaignRepository = campaignRepository;
        this.couponGeneratorService = couponGeneratorService;
    }

    @Scheduled(cron = "0 5 0 * * ?") // 12:05 AM every day
    @Transactional
    public void unlockNextRewards() {
        System.out.println("Running unlockNextRewards Job...");
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        List<CustomerReward> redeemedRewards = customerRewardRepository.findByStatusAndRedeemedAtBefore("REDEEMED", startOfToday);
        
        for (CustomerReward redeemed : redeemedRewards) {
            // Find all rewards for this user and campaign
            List<CustomerReward> userJourney = customerRewardRepository.findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(
                    redeemed.getCustomerId(), redeemed.getCampaignId());
            
            // Find the definition for the currently redeemed reward
            RewardDefinition currentDef = rewardDefinitionRepository.findById(redeemed.getRewardDefinitionId()).orElse(null);
            if (currentDef == null) continue;

            // Find the next definition (sequence + 1)
            int nextSeq = currentDef.getSequence() + 1;
            
            // Find if there is a LOCKED reward matching nextSeq
            for (CustomerReward cr : userJourney) {
                RewardDefinition def = rewardDefinitionRepository.findById(cr.getRewardDefinitionId()).orElse(null);
                if (def != null && def.getSequence() == nextSeq && "LOCKED".equals(cr.getStatus())) {
                    // Unlock it!
                    RewardCampaign campaign = campaignRepository.findById(cr.getCampaignId()).orElse(null);
                    if (campaign == null) continue;

                    cr.setStatus("ACTIVE");
                    cr.setCouponCode(couponGeneratorService.generateCoupon(campaign.getCampaignCode(), def.getSequence()));
                    cr.setActivatedAt(LocalDateTime.now());
                    cr.setExpiresAt(LocalDateTime.now().plusDays(def.getValidityDays()));
                    customerRewardRepository.save(cr);
                    
                    // WhatsApp message would be sent here (currently disabled for Phase 1)
                    System.out.println("Unlocked Reward " + nextSeq + " for customer " + cr.getCustomerId());
                    break;
                }
            }
        }
    }

    @Scheduled(cron = "0 0 10 * * ?") // 10:00 AM every day
    @Transactional
    public void markExpiredRewards() {
        System.out.println("Running markExpiredRewards Job...");
        List<CustomerReward> expiredRewards = customerRewardRepository.findByStatusAndExpiresAtBefore("ACTIVE", LocalDateTime.now());
        for (CustomerReward cr : expiredRewards) {
            cr.setStatus("EXPIRED");
            customerRewardRepository.save(cr);
            System.out.println("Expired Reward for customer " + cr.getCustomerId());
            // WhatsApp message for EXPIRED could be sent here
        }
        
        // Reminder WhatsApp logic would go here (currently disabled for Phase 1)
    }
}
