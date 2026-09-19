package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.CustomerReward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface CustomerRewardRepository extends JpaRepository<CustomerReward, Long> {
    List<CustomerReward> findByCustomerIdAndCampaignIdOrderByRewardDefinitionIdAsc(Long customerId, Long campaignId);
    Optional<CustomerReward> findByCouponCode(String couponCode);
    
    // For Admin Dashboard Stats
    long countByStatus(String status);
    List<CustomerReward> findTop50ByOrderByIdDesc();
    
    // For finding rewards that need to be unlocked today
    List<CustomerReward> findByStatus(String status);
    
    // Custom query to find REDEEMED rewards that were redeemed before a given time
    List<CustomerReward> findByStatusAndRedeemedAtBefore(String status, LocalDateTime date);
    
    // For expiry checks
    List<CustomerReward> findByStatusAndExpiresAtBefore(String status, LocalDateTime date);
    
    // For pending unlocks
    List<CustomerReward> findByStatusAndActivatedAtBefore(String status, LocalDateTime date);
    
    // For reminders
    List<CustomerReward> findByStatusAndExpiresAtBetweenAndReminderStatusIsNullOrReminderStatusNot(
        String status, LocalDateTime start, LocalDateTime end, String notReminderStatus);
}
