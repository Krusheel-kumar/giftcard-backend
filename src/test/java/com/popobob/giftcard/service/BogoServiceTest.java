package com.popobob.giftcard.service;

import com.popobob.giftcard.model.BogoCode;
import com.popobob.giftcard.model.CampaignUser;
import com.popobob.giftcard.repository.BogoCodeRepository;
import com.popobob.giftcard.repository.CampaignUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BogoServiceTest {

    @Mock
    private CampaignUserRepository campaignUserRepository;

    @Mock
    private BogoCodeRepository bogoCodeRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private WhatsAppService whatsappService;

    @InjectMocks
    private BogoService bogoService;

    @Test
    public void verify_TokenMobileMismatch_ThrowsException() {
        // Arrange
        String submittedMobile = "9999999999";
        String msg91VerifiedMobile = "8888888888";
        String token = "dummy-token";
        
        when(otpService.verifyToken(token)).thenReturn(msg91VerifiedMobile);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bogoService.verify(submittedMobile, "Test User", token);
        });

        assertEquals("Token mobile mismatch.", exception.getMessage());
        
        // Verify nothing was saved
        verify(campaignUserRepository, never()).save(any(CampaignUser.class));
        verify(bogoCodeRepository, never()).save(any(BogoCode.class));
    }

    @Test
    public void verify_MatchingMobile_Succeeds() {
        // Arrange
        String submittedMobile = "9999999999";
        String msg91VerifiedMobile = "919999999999"; // Will be normalized to 9999999999
        String token = "valid-token";
        
        when(otpService.verifyToken(token)).thenReturn(msg91VerifiedMobile);
        when(campaignUserRepository.findByMobileNumber("9999999999")).thenReturn(Optional.empty());
        
        BogoCode savedCode = new BogoCode();
        savedCode.setCode("BOGO-12345678");
        savedCode.setMobileNumber("9999999999");
        when(bogoCodeRepository.save(any(BogoCode.class))).thenReturn(savedCode);

        // Act
        BogoCode result = bogoService.verify(submittedMobile, "Test User", token);

        // Assert
        assertNotNull(result);
        assertEquals("9999999999", result.getMobileNumber());
        
        // Verify saved
        verify(campaignUserRepository, times(1)).save(any(CampaignUser.class));
        verify(bogoCodeRepository, times(1)).save(any(BogoCode.class));
        verify(whatsappService, times(1)).sendGiftCard(eq("9999999999"), eq("Test User"), eq("BOGO-12345678"));
    }
}
