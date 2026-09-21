package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.JourneyCustomer;
import com.popobob.giftcard.dto.AdminCustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JourneyCustomerRepository extends JpaRepository<JourneyCustomer, Long> {
    Optional<JourneyCustomer> findByMobile(String mobile);

    @Query("SELECT new com.popobob.giftcard.dto.AdminCustomerDTO(c.id, c.name, c.mobile, c.createdAt, " +
           "(SELECT COUNT(r) FROM CustomerReward r WHERE r.customerId = c.id AND r.status = 'REDEEMED')) " +
           "FROM JourneyCustomer c WHERE (:search IS NULL OR c.mobile LIKE CONCAT('%', :search, '%'))")
    Page<AdminCustomerDTO> findAdminCustomers(@Param("search") String search, Pageable pageable);
}
