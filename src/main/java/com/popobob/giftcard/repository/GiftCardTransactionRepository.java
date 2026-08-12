package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.GiftCardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Long> {
    
    List<GiftCardTransaction> findByGiftCardIdOrderByCreatedAtDesc(Long giftCardId);
    
    boolean existsByReferenceId(String referenceId);
}
