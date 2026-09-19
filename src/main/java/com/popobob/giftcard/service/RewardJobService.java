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
    private final RewardJourneyService rewardJourneyService;

    public RewardJobService(CustomerRewardRepository customerRewardRepository,
                            RewardDefinitionRepository rewardDefinitionRepository,
                            RewardCampaignRepository campaignRepository,
                            CouponGeneratorService couponGeneratorService,
                            RewardJourneyService rewardJourneyService) {
        this.customerRewardRepository = customerRewardRepository;
        this.rewardDefinitionRepository = rewardDefinitionRepository;
        this.campaignRepository = campaignRepository;
        this.couponGeneratorService = couponGeneratorService;
        this.rewardJourneyService = rewardJourneyService;
    }

    @Scheduled(cron = "0 0 10 * * ?") // 10:00 AM every day
    @Transactional
    public void markExpiredRewardsAndUnlockNext() {
        System.out.println("Running markExpiredRewards Job...");
        List<CustomerReward> expiredRewards = customerRewardRepository.findByStatusAndExpiresAtBefore("ACTIVE", LocalDateTime.now());
        for (CustomerReward cr : expiredRewards) {
            cr.setStatus("EXPIRED");
            customerRewardRepository.save(cr);
            System.out.println("Expired Reward for customer " + cr.getCustomerId());
            
            // Per Option A: Unlock the next reward so the user is not stuck
            rewardJourneyService.unlockNextRewardAndNotify(cr);
        }
    }

    @Scheduled(cron = "0 * * * * ?") // Every minute
    @Transactional
    public void processPendingUnlocks() {
        List<CustomerReward> pending = customerRewardRepository.findByStatusAndActivatedAtBefore("PENDING_UNLOCK", LocalDateTime.now());
        for (CustomerReward cr : pending) {
            System.out.println("24 hours passed! Unlocking reward for customer " + cr.getCustomerId());
            rewardJourneyService.executePendingUnlock(cr);
        }
    }
}
