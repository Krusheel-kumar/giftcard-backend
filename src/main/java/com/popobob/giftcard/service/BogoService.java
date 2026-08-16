package com.popobob.giftcard.service;

import com.popobob.giftcard.model.BogoCode;
import com.popobob.giftcard.model.CampaignUser;
import com.popobob.giftcard.repository.BogoCodeRepository;
import com.popobob.giftcard.repository.CampaignUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;
import com.popobob.giftcard.dto.AdminStatsDto;
import com.popobob.giftcard.dto.AdminRecordDto;

@Service
public class BogoService {
    @Autowired private CampaignUserRepository campaignUserRepository;
    @Autowired private BogoCodeRepository bogoCodeRepository;
    @Autowired private OtpService otpService;
    @Autowired private WhatsAppService whatsappService;
    
    private String normalizeMobile(String mobile) {
        if (mobile == null) return "";
        mobile = mobile.replaceAll("[^0-9]", "");
        return mobile.length() > 10 ? mobile.substring(mobile.length() - 10) : mobile;
    }
    
    public void claim(String mobileNumber) {
        String normalized = normalizeMobile(mobileNumber);
        if (campaignUserRepository.findByMobileNumber(normalized).isPresent()) {
            throw new RuntimeException("Mobile number already claimed this offer.");
        }
    }
    
    public BogoCode verify(String mobileNumber, String customerName, String token) {
        String normalized = normalizeMobile(mobileNumber);
        String verifiedMobile = otpService.verifyToken(token);
        String normalizedVerified = normalizeMobile(verifiedMobile);
        
        if (!normalized.equals(normalizedVerified)) {
            throw new RuntimeException("Token mobile mismatch.");
        }
        
        if (campaignUserRepository.findByMobileNumber(normalized).isPresent()) {
            throw new RuntimeException("Mobile number already claimed this offer.");
        }
        
        CampaignUser user = new CampaignUser();
        user.setMobileNumber(normalized);
        user.setName(customerName);
        campaignUserRepository.save(user);
        
        BogoCode code = new BogoCode();
        code.setCode("BOGO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        code.setMobileNumber(normalized);
        code.setStatus("ACTIVE");
        BogoCode savedCode = bogoCodeRepository.save(code);
        
        // Trigger WhatsApp Message
        whatsappService.sendGiftCard(normalized, customerName, savedCode.getCode());
        
        return savedCode;
    }
    
    public Map<String, Object> validate(String codeStr, String storeId, List<Map<String, Object>> cartItems) {
        BogoCode code = bogoCodeRepository.findByCode(codeStr)
            .orElseThrow(() -> new RuntimeException("Code not found"));
        if (!"ACTIVE".equals(code.getStatus())) throw new RuntimeException("Code is no longer active (already redeemed).");
        if (!"1".equals(storeId)) throw new RuntimeException("This BOGO offer is only valid at the Film Nagar store.");
        
        List<Double> items = new ArrayList<>();
        for (Map<String, Object> item : cartItems) {
            double price = Double.parseDouble(item.get("price").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());
            for (int i = 0; i < quantity; i++) items.add(price);
        }
        if (items.size() < 2) throw new RuntimeException("Minimum 2 items required in cart to apply Buy 1 Get 1 Free.");
        
        items.sort(Collections.reverseOrder());
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("discountAmount", items.get(1)); // The second most expensive item is free
        return response;
    }
    
    public void redeem(String codeStr) {
        BogoCode code = bogoCodeRepository.findByCode(codeStr)
            .orElseThrow(() -> new RuntimeException("Code not found"));
        code.setStatus("REDEEMED");
        code.setRedeemedAt(LocalDateTime.now());
        bogoCodeRepository.save(code);
    }
    
    public BogoCode lookupAdmin(String codeStr) {
        return bogoCodeRepository.findByCode(codeStr)
            .orElseThrow(() -> new RuntimeException("Code not found"));
    }
    
    public AdminStatsDto getAdminStats() {
        List<BogoCode> allCodes = bogoCodeRepository.findAll();
        List<CampaignUser> allUsers = campaignUserRepository.findAll();
        
        Map<String, String> mobileToName = new HashMap<>();
        for (CampaignUser u : allUsers) {
            mobileToName.put(u.getMobileNumber(), u.getName());
        }
        
        long generated = allCodes.size();
        long redeemed = allCodes.stream().filter(c -> "REDEEMED".equals(c.getStatus())).count();
        
        List<AdminRecordDto> records = new ArrayList<>();
        for (BogoCode c : allCodes) {
            AdminRecordDto dto = new AdminRecordDto();
            dto.setMobileNumber(c.getMobileNumber());
            dto.setName(mobileToName.getOrDefault(c.getMobileNumber(), "Unknown"));
            dto.setCode(c.getCode());
            dto.setStatus(c.getStatus());
            dto.setGeneratedAt(c.getCreatedAt());
            dto.setRedeemedAt(c.getRedeemedAt());
            records.add(dto);
        }
        
        records.sort((a, b) -> {
            if (a.getGeneratedAt() == null && b.getGeneratedAt() == null) return 0;
            if (a.getGeneratedAt() == null) return 1;
            if (b.getGeneratedAt() == null) return -1;
            return b.getGeneratedAt().compareTo(a.getGeneratedAt());
        });
        
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalGenerated(generated);
        stats.setTotalRedeemed(redeemed);
        stats.setRecords(records);
        return stats;
    }
}
