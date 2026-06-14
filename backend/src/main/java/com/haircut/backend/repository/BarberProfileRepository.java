package com.haircut.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.BarberProfile;

public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {

}
