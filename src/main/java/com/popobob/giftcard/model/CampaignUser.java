package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CampaignUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String mobileNumber;
}
