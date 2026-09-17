package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.RewardDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RewardDefinitionRepository extends JpaRepository<RewardDefinition, Long> {
    List<RewardDefinition> findByCampaignIdOrderBySequenceAsc(Long campaignId);
}
