package com.popobob.giftcard.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PurchaseRequest {
    @NotBlank(message = "Purchaser name is required")
    private String purchaserName;
    
    @NotBlank(message = "Purchaser mobile is required")
    private String purchaserMobile;
    
    private String recipientName;
    private String recipientMobile;
    private String personalMessage;
}
