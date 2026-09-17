package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.JourneyCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JourneyCustomerRepository extends JpaRepository<JourneyCustomer, Long> {
    Optional<JourneyCustomer> findByMobile(String mobile);
}
