package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.BogoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BogoCodeRepository extends JpaRepository<BogoCode, Long> {
    Optional<BogoCode> findByCode(String code);
}
