package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reward_definitions")
public class RewardDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long campaignId;

    private Integer sequence; // 1 to 4
    
    private String name;
    private String description;
    
    private String rewardType; // OFFER, PERCENTAGE, COMBO, FREE_ITEM
    private String rewardValue; 
    
    private Integer validityDays;
    
    private boolean active;
}
