package com.popobob.giftcard.service;

import com.popobob.giftcard.exception.GiftCardException;
import com.popobob.giftcard.exception.GiftCardNotFoundException;
import com.popobob.giftcard.model.GiftCard;
import com.popobob.giftcard.model.GiftCardStatus;
import com.popobob.giftcard.model.GiftCardTransaction;
import com.popobob.giftcard.model.TransactionType;
import com.popobob.giftcard.repository.GiftCardRepository;
import com.popobob.giftcard.repository.GiftCardTransactionRepository;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository transactionRepository;

    @Value("${razorpay.key-id:rzp_test_T4aQ5u6TRc7G0O}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:wds1TgsHLGs5qdrb7NwmAhtN}")
    private String razorpayKeySecret;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluded I, O, 1, 0 for readability
    private static final int CODE_LENGTH = 8;
    private final SecureRandom secureRandom = new SecureRandom();

    // ─────────────────────────────────────────────────────────────
    // PURCHASE FLOW
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a Razorpay order and a PENDING gift card record.
     */
    @Transactional
    public GiftCard initiatePurchase(String purchaserName, String purchaserMobile,
                                     String recipientName, String recipientMobile,
                                     String personalMessage) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 20000); // ₹200 in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "gc_" + System.currentTimeMillis());

            com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id").toString();

            GiftCard giftCard = GiftCard.builder()
                    .publicCode(generateUniqueCode())
                    .shareToken(generateShareToken())
                    .purchaseAmount(new BigDecimal("200.00"))
                    .initialBalance(new BigDecimal("300.00"))
                    .currentBalance(new BigDecimal("300.00"))
                    .purchaserName(purchaserName)
                    .purchaserMobile(purchaserMobile)
                    .recipientName(recipientName)
                    .recipientMobile(recipientMobile)
                    .personalMessage(personalMessage)
                    .status(GiftCardStatus.PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .expiresAt(LocalDateTime.now().plusYears(1))
                    .build();

            return giftCardRepository.save(giftCard);

        } catch (Exception e) {
            log.error("Failed to initiate gift card purchase: {}", e.getMessage());
            throw new GiftCardException("Failed to initiate purchase: " + e.getMessage());
        }
    }

    /**
     * Verifies Razorpay signature and activates the gift card.
     */
    @Transactional
    public GiftCard verifyAndActivate(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        // 1. Verify signature (CRITICAL - never skip)
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            boolean isValid = com.razorpay.Utils.verifyPaymentSignature(
                    new JSONObject()
                            .put("razorpay_order_id", razorpayOrderId)
                            .put("razorpay_payment_id", razorpayPaymentId)
                            .put("razorpay_signature", razorpaySignature),
                    razorpayKeySecret
            );
            if (!isValid) {
                log.error("Razorpay signature verification FAILED for order: {}", razorpayOrderId);
                throw new GiftCardException("Payment signature verification failed. Possible tampering detected.");
            }
        } catch (com.razorpay.RazorpayException e) {
            log.error("Razorpay verification exception: {}", e.getMessage());
            throw new GiftCardException("Payment verification error: " + e.getMessage());
        }

        // 2. Find PENDING gift card by order ID
        GiftCard giftCard = giftCardRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new GiftCardNotFoundException("Gift Card not found for order: " + razorpayOrderId));

        // 3. Idempotency: already activated? Return existing card
        if (giftCard.getStatus() == GiftCardStatus.ACTIVE) {
            log.warn("Gift Card already activated for order: {} — returning existing card", razorpayOrderId);
            return giftCard;
        }

        if (giftCard.getStatus() != GiftCardStatus.PENDING) {
            throw new GiftCardException("Gift Card is not in PENDING status. Current: " + giftCard.getStatus());
        }

        // 4. Activate
        giftCard.setStatus(GiftCardStatus.ACTIVE);
        giftCard.setRazorpayPaymentId(razorpayPaymentId);
        GiftCard savedCard = giftCardRepository.save(giftCard);

        // 5. Record ISSUE transaction
        GiftCardTransaction transaction = GiftCardTransaction.builder()
                .giftCard(savedCard)
                .transactionType(TransactionType.ISSUE)
                .amount(savedCard.getInitialBalance())
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(savedCard.getInitialBalance())
                .referenceId("ISSUE_" + razorpayPaymentId)
                .build();
        transactionRepository.save(transaction);

        log.info("Gift Card ACTIVATED — Code: [REDACTED], Payment: {}", razorpayPaymentId);
        return savedCard;
    }

    // ─────────────────────────────────────────────────────────────
    // SHARE PAGE
    // ─────────────────────────────────────────────────────────────

    public GiftCard getByShareToken(String shareToken) {
        return giftCardRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new GiftCardNotFoundException("Invalid or expired share link."));
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION (for staff — before redemption)
    // ─────────────────────────────────────────────────────────────

    public GiftCard validateCard(String publicCode) {
        GiftCard giftCard = giftCardRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new GiftCardNotFoundException("Gift Card not found: " + publicCode));

        if (giftCard.getStatus() != GiftCardStatus.ACTIVE) {
            throw new GiftCardException("Card is not active. Status: " + giftCard.getStatus());
        }
        if (giftCard.getExpiresAt() != null && giftCard.getExpiresAt().isBefore(LocalDateTime.now())) {
            giftCard.setStatus(GiftCardStatus.EXPIRED);
            giftCardRepository.save(giftCard);
            throw new GiftCardException("Gift Card has expired.");
        }
        return giftCard;
    }

    // ─────────────────────────────────────────────────────────────
    // REDEMPTION
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public GiftCardTransaction redeem(String publicCode, BigDecimal amount, Long staffId, Long storeId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GiftCardException("Redemption amount must be greater than zero");
        }

        // Pessimistic lock — prevents concurrent double-redemption
        GiftCard giftCard = giftCardRepository.findByPublicCodeForUpdate(publicCode)
                .orElseThrow(() -> new GiftCardNotFoundException("Gift Card not found"));

        if (giftCard.getStatus() != GiftCardStatus.ACTIVE) {
            throw new GiftCardException("Gift Card is not active. Status: " + giftCard.getStatus());
        }
        if (giftCard.getExpiresAt() != null && giftCard.getExpiresAt().isBefore(LocalDateTime.now())) {
            giftCard.setStatus(GiftCardStatus.EXPIRED);
            giftCardRepository.save(giftCard);
            throw new GiftCardException("Gift Card has expired");
        }
        if (giftCard.getCurrentBalance().compareTo(amount) < 0) {
            throw new GiftCardException("Insufficient balance. Available: ₹" + giftCard.getCurrentBalance());
        }

        BigDecimal balanceBefore = giftCard.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        giftCard.setCurrentBalance(balanceAfter);
        if (balanceAfter.compareTo(BigDecimal.ZERO) == 0) {
            giftCard.setStatus(GiftCardStatus.FULLY_REDEEMED);
        }
        giftCardRepository.save(giftCard);

        GiftCardTransaction transaction = GiftCardTransaction.builder()
                .giftCard(giftCard)
                .transactionType(TransactionType.REDEEM)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceId(UUID.randomUUID().toString())
                .staffId(staffId)
                .storeId(storeId)
                .build();

        log.info("Redeemed ₹{} from card ID {} — Balance: ₹{} → ₹{}", amount, giftCard.getId(), balanceBefore, balanceAfter);
        return transactionRepository.save(transaction);
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────────────────────

    public List<GiftCard> getAllCards() {
        return giftCardRepository.findAll();
    }

    public GiftCard getCardById(Long id) {
        return giftCardRepository.findById(id)
                .orElseThrow(() -> new GiftCardNotFoundException("Gift Card not found: " + id));
    }

    public List<GiftCardTransaction> getTransactions(Long giftCardId) {
        return transactionRepository.findByGiftCardIdOrderByCreatedAtDesc(giftCardId);
    }

    @Transactional
    public GiftCard blockCard(Long id) {
        GiftCard card = giftCardRepository.findById(id)
                .orElseThrow(() -> new GiftCardNotFoundException("Gift Card not found: " + id));
        card.setStatus(GiftCardStatus.BLOCKED);
        return giftCardRepository.save(card);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("POB-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                if (i == 4) sb.append("-");
                sb.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (giftCardRepository.findByPublicCode(code).isPresent());
        return code;
    }

    private String generateShareToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (giftCardRepository.findByShareToken(token).isPresent());
        return token;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}
