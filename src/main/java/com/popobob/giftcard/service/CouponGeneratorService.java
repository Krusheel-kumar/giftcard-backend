package com.popobob.giftcard.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class CouponGeneratorService {
    
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed ambiguous chars I, O, 1, 0
    private static final int CODE_LENGTH = 5;
    private final SecureRandom random = new SecureRandom();

    public String generateCoupon(String campaignCode, int sequence) {
        // e.g. POBFN-1-8K72Q
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return campaignCode + "-" + sequence + "-" + code.toString();
    }
}
