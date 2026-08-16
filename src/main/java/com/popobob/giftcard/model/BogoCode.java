package com.popobob.giftcard.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
public class BogoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String code;
    private String mobileNumber;
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime redeemedAt;
}
