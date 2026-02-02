package com.yourorg.portfolio.repository;

import com.yourorg.portfolio.model.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, Long> {
    Optional<RiskProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

