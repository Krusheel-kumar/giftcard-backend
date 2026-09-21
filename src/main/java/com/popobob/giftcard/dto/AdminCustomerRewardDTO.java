package com.popobob.giftcard.dto;

import java.time.LocalDateTime;

public class AdminCustomerRewardDTO {
    private String rewardName;
    private String couponCode;
    private String status;
    private LocalDateTime activatedAt;
    private LocalDateTime redeemedAt;
    private LocalDateTime expiresAt;

    public AdminCustomerRewardDTO(String rewardName, String couponCode, String status, LocalDateTime activatedAt, LocalDateTime redeemedAt, LocalDateTime expiresAt) {
        this.rewardName = rewardName;
        this.couponCode = couponCode;
        this.status = status;
        this.activatedAt = activatedAt;
        this.redeemedAt = redeemedAt;
        this.expiresAt = expiresAt;
    }

    public String getRewardName() { return rewardName; }
    public String getCouponCode() { return couponCode; }
    public String getStatus() { return status; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
