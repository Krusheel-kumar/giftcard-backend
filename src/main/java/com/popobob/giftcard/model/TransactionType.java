package com.popobob.giftcard.model;

public enum TransactionType {
    ISSUE,          // Initial credit when purchased
    REDEEM,         // Debit when redeemed at a store
    ADJUSTMENT,     // Admin manual adjustment
    REFUND          // Refund process
}
