package com.haircut.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.ServiceTierPrice;
import com.haircut.backend.entity.Tier;

public interface ServiceTierPriceRepository extends JpaRepository<ServiceTierPrice, Long> {
  List<ServiceTierPrice> findByServiceId(Long serviceId);
  Optional<ServiceTierPrice> findByServiceIdAndTier(Long serviceId, Tier tier);
  boolean existsByServiceIdAndTier(Long serviceId, Tier tier);
}
