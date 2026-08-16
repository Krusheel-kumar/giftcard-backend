package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.BogoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface BogoCodeRepository extends JpaRepository<BogoCode, Long> {
    Optional<BogoCode> findByCode(String code);

    @Modifying
    @Query("UPDATE BogoCode c SET c.status = 'REDEEMED', c.redeemedAt = :now WHERE c.code = :code AND c.status = 'ACTIVE'")
    int redeemCodeAtomically(@Param("code") String code, @Param("now") LocalDateTime now);
}
