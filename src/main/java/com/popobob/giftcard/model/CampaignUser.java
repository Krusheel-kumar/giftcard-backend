package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
public class CampaignUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String mobileNumber;
    private String name;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
