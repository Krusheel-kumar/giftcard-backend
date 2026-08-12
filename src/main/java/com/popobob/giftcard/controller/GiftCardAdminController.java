package com.popobob.giftcard.controller;

import com.popobob.giftcard.dto.RedeemRequest;
import com.popobob.giftcard.model.GiftCard;
import com.popobob.giftcard.model.GiftCardTransaction;
import com.popobob.giftcard.service.GiftCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/gift-cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GiftCardAdminController {

    private final GiftCardService giftCardService;

    /**
     * Get all gift cards — admin dashboard overview.
     */
    @GetMapping
    public ResponseEntity<?> getAllCards() {
        List<GiftCard> cards = giftCardService.getAllCards();
        return ResponseEntity.ok(cards.stream().map(c -> Map.of(
                "id", c.getId(),
                "publicCode", c.getPublicCode(),
                "status", c.getStatus().name(),
                "currentBalance", c.getCurrentBalance(),
                "initialBalance", c.getInitialBalance(),
                "purchaserName", c.getPurchaserName(),
                "recipientName", c.getRecipientName() != null ? c.getRecipientName() : "",
                "createdAt", c.getCreatedAt().toString()
        )).collect(Collectors.toList()));
    }

    /**
     * Get a single card detail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCard(@PathVariable Long id) {
        try {
            GiftCard c = giftCardService.getCardById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", c.getId());
            response.put("publicCode", c.getPublicCode());
            response.put("status", c.getStatus().name());
            response.put("purchaseAmount", c.getPurchaseAmount());
            response.put("initialBalance", c.getInitialBalance());
            response.put("currentBalance", c.getCurrentBalance());
            response.put("purchaserName", c.getPurchaserName());
            response.put("purchaserMobile", c.getPurchaserMobile());
            response.put("recipientName", c.getRecipientName() != null ? c.getRecipientName() : "");
            response.put("recipientMobile", c.getRecipientMobile() != null ? c.getRecipientMobile() : "");
            response.put("personalMessage", c.getPersonalMessage() != null ? c.getPersonalMessage() : "");
            response.put("razorpayOrderId", c.getRazorpayOrderId() != null ? c.getRazorpayOrderId() : "");
            response.put("expiresAt", c.getExpiresAt() != null ? c.getExpiresAt().toString() : "");
            response.put("createdAt", c.getCreatedAt().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Get all transactions for a card.
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long id) {
        List<GiftCardTransaction> txns = giftCardService.getTransactions(id);
        return ResponseEntity.ok(txns.stream().map(t -> Map.of(
                "id", t.getId(),
                "type", t.getTransactionType().name(),
                "amount", t.getAmount(),
                "balanceBefore", t.getBalanceBefore(),
                "balanceAfter", t.getBalanceAfter(),
                "createdAt", t.getCreatedAt().toString()
        )).collect(Collectors.toList()));
    }

    /**
     * Block a card.
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long id) {
        try {
            GiftCard blocked = giftCardService.blockCard(id);
            return ResponseEntity.ok(Map.of("success", true, "status", blocked.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Step 1 of Staff redemption: Validate the code before asking for amount.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateCard(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");
            if (code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Code is required"));
            }
            GiftCard card = giftCardService.validateCard(code.toUpperCase().trim());
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "balance", card.getCurrentBalance(),
                    "recipientName", card.getRecipientName() != null ? card.getRecipientName() : "",
                    "status", card.getStatus().name(),
                    "expiresAt", card.getExpiresAt() != null ? card.getExpiresAt().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    /**
     * Step 2 of Staff redemption: Deduct balance atomically.
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCard(@Valid @RequestBody RedeemRequest request) {
        try {
            Long staffId = 1L; // TODO: extract from JWT SecurityContext
            GiftCardTransaction transaction = giftCardService.redeem(
                    request.getCode().toUpperCase().trim(),
                    request.getAmount(),
                    staffId,
                    request.getStoreId()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redeemedAmount", transaction.getAmount(),
                    "remainingBalance", transaction.getBalanceAfter(),
                    "transactionId", transaction.getId(),
                    "message", "Redemption successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
