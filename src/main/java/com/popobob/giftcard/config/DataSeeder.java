package com.popobob.giftcard.config;

import com.popobob.giftcard.model.RewardCampaign;
import com.popobob.giftcard.model.RewardDefinition;
import com.popobob.giftcard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RewardCampaignRepository campaignRepository;
    private final RewardDefinitionRepository rewardDefinitionRepository;
    private final JourneyCustomerRepository journeyCustomerRepository;
    private final CustomerRewardRepository customerRewardRepository;

    public DataSeeder(RewardCampaignRepository campaignRepository, 
                      RewardDefinitionRepository rewardDefinitionRepository,
                      JourneyCustomerRepository journeyCustomerRepository,
                      CustomerRewardRepository customerRewardRepository) {
        this.campaignRepository = campaignRepository;
        this.rewardDefinitionRepository = rewardDefinitionRepository;
        this.journeyCustomerRepository = journeyCustomerRepository;
        this.customerRewardRepository = customerRewardRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (campaignRepository.count() == 0) {
            System.out.println("No campaigns found. Seeding new 3-reward campaign...");

            // Wipe all old data
            customerRewardRepository.deleteAll();
            journeyCustomerRepository.deleteAll();
            rewardDefinitionRepository.deleteAll();
            campaignRepository.deleteAll();

            // Create Campaign
            RewardCampaign campaign = new RewardCampaign();
            campaign.setCampaignCode("POBFN");
            campaign.setCampaignName("Film Nagar Launch");
            campaign.setStoreId("FILM_NAGAR");
            campaign.setActive(true);
            campaign.setStartDate(LocalDate.now());
            campaign.setEndDate(LocalDate.now().plusMonths(6));
            campaign = campaignRepository.save(campaign);

            // Create the 3 New Rewards
            createReward(campaign.getId(), 1, "Buy 1 & Get 1", "Buy 1 Boba, get 1 free", "BOGO", "1", 10);
            createReward(campaign.getId(), 2, "20% OFF", "Get 20% off your entire order", "PERCENTAGE", "20", 30);
            createReward(campaign.getId(), 3, "Free Boba", "Milestone Reward: Get any Boba drink absolutely free!", "FREE_ITEM", "BOBA", 30);

            System.out.println("Seeding complete! 3 Rewards Created.");
        }
    }

    private void createReward(Long campaignId, int sequence, String name, String desc, String type, String value, int validityDays) {
        RewardDefinition def = new RewardDefinition();
        def.setCampaignId(campaignId);
        def.setSequence(sequence);
        def.setName(name);
        def.setDescription(desc);
        def.setRewardType(type);
        def.setRewardValue(value);
        def.setValidityDays(validityDays);
        def.setActive(true);
        rewardDefinitionRepository.save(def);
    }
}
