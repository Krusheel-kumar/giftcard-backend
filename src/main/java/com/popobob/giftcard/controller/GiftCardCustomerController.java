package com.popobob.giftcard.controller;

import com.popobob.giftcard.dto.PurchaseRequest;
import com.popobob.giftcard.model.GiftCard;
import com.popobob.giftcard.service.GiftCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gift-cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GiftCardCustomerController {

    private final GiftCardService giftCardService;

    /**
     * Step 1: Customer submits form → backend creates Razorpay order + PENDING card.
     * Returns the Razorpay order details for checkout.js.
     */
    @PostMapping("/purchase")
    public ResponseEntity<?> initiatePurchase(@Valid @RequestBody PurchaseRequest request) {
        try {
            GiftCard pendingCard = giftCardService.initiatePurchase(
                    request.getPurchaserName(),
                    request.getPurchaserMobile(),
                    request.getRecipientName(),
                    request.getRecipientMobile(),
                    request.getPersonalMessage()
            );

            return ResponseEntity.ok(Map.of(
                    "giftCardId", pendingCard.getId(),
                    "razorpayOrderId", pendingCard.getRazorpayOrderId(),
                    "razorpayKeyId", giftCardService.getRazorpayKeyId(),
                    "amount", 20000,
                    "currency", "INR",
                    "purchaserName", pendingCard.getPurchaserName(),
                    "purchaserMobile", pendingCard.getPurchaserMobile()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Step 2: Razorpay succeeds → frontend sends signature → backend verifies → activates card.
     */
    @PostMapping("/payment/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String razorpayOrderId   = payload.get("razorpay_order_id");
            String razorpayPaymentId = payload.get("razorpay_payment_id");
            String razorpaySignature = payload.get("razorpay_signature");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing payment fields"));
            }

            GiftCard activeCard = giftCardService.verifyAndActivate(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "publicCode", activeCard.getPublicCode(),
                    "shareToken", activeCard.getShareToken(),
                    "recipientName", activeCard.getRecipientName() != null ? activeCard.getRecipientName() : "",
                    "personalMessage", activeCard.getPersonalMessage() != null ? activeCard.getPersonalMessage() : "",
                    "balance", activeCard.getCurrentBalance(),
                    "expiresAt", activeCard.getExpiresAt().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Shareable recipient page — returns public card info only.
     */
    @GetMapping("/share/{token}")
    public ResponseEntity<?> getSharePage(@PathVariable String token) {
        try {
            GiftCard card = giftCardService.getByShareToken(token);

            // Only expose what the recipient needs to see — never expose internal IDs
            return ResponseEntity.ok(Map.of(
                    "recipientName", card.getRecipientName() != null ? card.getRecipientName() : "Friend",
                    "purchaserName", card.getPurchaserName(),
                    "personalMessage", card.getPersonalMessage() != null ? card.getPersonalMessage() : "",
                    "balance", card.getCurrentBalance(),
                    "publicCode", card.getPublicCode(),
                    "status", card.getStatus().name(),
                    "expiresAt", card.getExpiresAt().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
