package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.GiftCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {

    Optional<GiftCard> findByPublicCode(String publicCode);

    Optional<GiftCard> findByRazorpayOrderId(String razorpayOrderId);

    Optional<GiftCard> findByShareToken(String shareToken);

    // Used during redemption to prevent concurrent modifications
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GiftCard g WHERE g.publicCode = :publicCode")
    Optional<GiftCard> findByPublicCodeForUpdate(@Param("publicCode") String publicCode);
}
