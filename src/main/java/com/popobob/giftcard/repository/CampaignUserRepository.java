package com.popobob.giftcard.repository;

import com.popobob.giftcard.model.CampaignUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CampaignUserRepository extends JpaRepository<CampaignUser, Long> {
    Optional<CampaignUser> findByMobileNumber(String mobileNumber);
}
