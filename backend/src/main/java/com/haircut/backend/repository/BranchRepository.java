package com.haircut.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.haircut.backend.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {

}
