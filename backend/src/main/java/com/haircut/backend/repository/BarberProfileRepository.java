package com.haircut.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.BarberProfile;

public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {
  List<BarberProfile> findByBranchId(Long branchId);
}
