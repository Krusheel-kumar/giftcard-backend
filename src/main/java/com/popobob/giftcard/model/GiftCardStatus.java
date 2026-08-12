package com.popobob.giftcard.model;

public enum GiftCardStatus {
    PENDING,        // Payment initiated but not confirmed
    ACTIVE,         // Payment confirmed, card is active
    BLOCKED,        // Admin blocked the card
    EXPIRED,        // Card validity expired
    FULLY_REDEEMED  // Balance is 0
}
