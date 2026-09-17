package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.RewardCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RewardCampaignRepository extends JpaRepository<RewardCampaign, Long> {
    Optional<RewardCampaign> findByCampaignCode(String campaignCode);
}
