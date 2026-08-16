package com.popobob.giftcard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminRecordDto {
    private String name;
    private String mobileNumber;
    private String code;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime redeemedAt;
}
