package com.haircut.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.CreateBarberProfileRequest;
import com.haircut.backend.dto.UpdateBarberProfileRequest;
import com.haircut.backend.entity.BarberProfile;
import com.haircut.backend.entity.Branch;
import com.haircut.backend.entity.User;
import com.haircut.backend.repository.BarberProfileRepository;
import com.haircut.backend.repository.BranchRepository;
import com.haircut.backend.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/barber-profiles")
public class BarberProfileController {
  private final BarberProfileRepository barberProfileRepository;
  private final UserRepository userRepository;
  private final BranchRepository branchRepository;

  public BarberProfileController(
      BarberProfileRepository barberProfileRepository,
      UserRepository userRepository,
      BranchRepository branchRepository) {
    this.barberProfileRepository = barberProfileRepository;
    this.userRepository = userRepository;
    this.branchRepository = branchRepository;
  }

  @GetMapping()
  public ResponseEntity<List<BarberProfile>> getAllBarberProfiles() {
    List<BarberProfile> data = barberProfileRepository.findAll();
    return ResponseEntity.status(HttpStatus.OK).body(data);
  }

  // === GET /barber-profiles/{id} ===
  @GetMapping("/{id}")
  public ResponseEntity<BarberProfile> getBarberProfileById(@PathVariable Long id) {
    Optional<BarberProfile> result = barberProfileRepository.findById(id);
    if (result.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(result.get());
  }

  @PostMapping
  public ResponseEntity<BarberProfile> createBarberProfile(
      @RequestBody CreateBarberProfileRequest req) {

    // Bước 1: Lookup User
    // userRepository.findById(req.userId()) → Optional
    // Nếu không tồn tại → return 404 (hoặc 400 Bad Request)
    User user = userRepository.findById(req.userId()).orElseThrow();

    // Bước 2: Lookup Branch tương tự
    Branch branch = branchRepository.findById(req.branchId()).orElseThrow();

    // Bước 3: Tạo BarberProfile mới
    // new BarberProfile(user, branch, req.tier())
    // set thêm bio, yearsExp (có thể null thì dùng default)
    BarberProfile profile = new BarberProfile(user, branch, req.tier());
    profile.setBio(req.bio());
    profile.setYearsExp(req.yearsExp() != null ? req.yearsExp() : 0);

    // Bước 4: Save + trả 201 Created
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(barberProfileRepository.save(profile));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BarberProfile> updateBarberProfile(@PathVariable Long id,
      @Valid @RequestBody UpdateBarberProfileRequest req) {
    Optional<BarberProfile> result = barberProfileRepository.findById(id);
    if (result.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    BarberProfile barberProfileExisting = result.get();
    Branch branch = branchRepository.findById(req.branchId()).orElseThrow();
    barberProfileExisting.setBranch(branch);
    barberProfileExisting.setTier(req.tier());
    barberProfileExisting.setBio(req.bio() != null ? req.bio() : barberProfileExisting.getBio());
    barberProfileExisting.setYearsExp(req.yearsExp() != null ? req.yearsExp() : 0);
    barberProfileExisting
        .setRatingAvg(req.ratingAvg() != null ? req.ratingAvg() : barberProfileExisting.getRatingAvg());
    barberProfileExisting.setActive(req.active() != null ? req.active() : false);

    barberProfileRepository.save(barberProfileExisting);
    return ResponseEntity.status(HttpStatus.OK).body(barberProfileExisting);

  }

  // === DELETE /barber-profiles/{id} ===
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBarberProfileById(@PathVariable Long id) {
    if (!barberProfileRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    barberProfileRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
