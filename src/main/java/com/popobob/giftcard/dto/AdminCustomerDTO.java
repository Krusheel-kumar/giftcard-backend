package com.popobob.giftcard.dto;

import java.time.LocalDateTime;

public class AdminCustomerDTO {
    private Long id;
    private String name;
    private String mobile;
    private LocalDateTime createdAt;
    private Long totalRedeemed;

    public AdminCustomerDTO(Long id, String name, String mobile, LocalDateTime createdAt, Long totalRedeemed) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.createdAt = createdAt;
        this.totalRedeemed = totalRedeemed;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getTotalRedeemed() { return totalRedeemed; }
}
