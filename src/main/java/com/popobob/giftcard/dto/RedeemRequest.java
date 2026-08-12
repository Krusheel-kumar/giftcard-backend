package com.popobob.giftcard.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class RedeemRequest {
    @NotBlank(message = "Gift Card code is required")
    private String code;
    
    @NotNull(message = "Redemption amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private Long storeId;
}
