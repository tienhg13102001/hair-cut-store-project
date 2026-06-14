package com.haircut.backend.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "barber_profiles")
public class BarberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Quan hệ @OneToOne với User ===
    // 1 BarberProfile gắn với đúng 1 User (user role = BARBER).
    // - fetch = LAZY: chỉ load User khi gọi getUser().getXxx()
    // - @JoinColumn: cột FK trong bảng barber_profiles tên là "user_id"
    // - unique = true: đảm bảo 1 User chỉ có 1 BarberProfile
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // === Quan hệ @ManyToOne với Branch ===
    // Nhiều BarberProfile thuộc 1 Branch.
    // - Cột FK "branch_id" trong bảng barber_profiles
    // - LAZY để tránh kéo Branch về mỗi lần load BarberProfile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Tier tier;

    // === bio dùng TEXT thay vì VARCHAR(255) mặc định ===
    // columnDefinition là "chỉ thị thô" tới DB. Chỉ dùng khi default không đủ.
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "years_exp", nullable = false)
    private int yearsExp = 0;

    // === BigDecimal cho rating ===
    // precision = 3: tổng 3 chữ số (vd 4.85)
    // scale = 2: 2 chữ số sau dấu phẩy
    // KHÔNG dùng double/float cho số liệu cần chính xác.
    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // === Constructors ===
    public BarberProfile() {
    }

    public BarberProfile(User user, Branch branch, Tier tier) {
        this.user = user;
        this.branch = branch;
        this.tier = tier;
    }

    // === Getters & Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(int yearsExp) {
        this.yearsExp = yearsExp;
    }

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
