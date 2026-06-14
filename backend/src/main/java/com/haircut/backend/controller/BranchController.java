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

import com.haircut.backend.entity.Branch;
import com.haircut.backend.repository.BranchRepository;

@RestController
@RequestMapping("/branches")
public class BranchController {

  private final BranchRepository branchRepository;

  public BranchController(BranchRepository branchRepository) {
    this.branchRepository = branchRepository;
  }

  // === 1. GET /branches ===
  @GetMapping
  public ResponseEntity<List<Branch>> getAll() {
    List<Branch> result = branchRepository.findAll();
    if (result.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.status(HttpStatus.FOUND).body(result);
  }

  // === 2. POST /branches ===
  @PostMapping
  public ResponseEntity<Branch> create(@RequestBody Branch branch) {
    Branch saved = branchRepository.save(branch);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  // TODO: bạn viết tiếp 3 method:
  // GET /branches/{id} → getById(Long id)
  // PUT /branches/{id} → update(Long id, Branch branch)
  // DELETE /branches/{id} → delete(Long id)

  @GetMapping("/{id}")
  public ResponseEntity<Branch> getBranchById(@PathVariable Long id) {
    Optional<Branch> result = branchRepository.findById(id);
    if (result.isPresent()) {
      return ResponseEntity.ok(result.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Branch> updateBranchById(@PathVariable Long id, @RequestBody Branch input) {
    // === Bước 1: Tìm chi nhánh hiện tại trong DB ===
    Optional<Branch> result = branchRepository.findById(id);
    // === Bước 2: Nếu không tồn tại → trả 404 và dừng ===
    if (result.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    // === Bước 3: Lấy entity ra khỏi Optional ===
    // An toàn vì ta vừa check isEmpty() = false ở trên
    Branch existing = result.get();

    // === Bước 4: Copy từng field từ `input` sang `existing` ===
    // Chỉ copy các field "business" (do người dùng nhập)
    // KHÔNG copy: id (đã có), createdAt (giữ nguyên), updatedAt (Hibernate tự
    // update)
    existing.setName(input.getName());
    existing.setAddress(input.getAddress());
    existing.setPhone(input.getPhone());
    existing.setOpeningAt(input.getOpeningAt());
    existing.setClosingAt(input.getClosingAt());
    existing.setActive(input.isActive());

    // === Bước 5: Save xuống DB ===
    // save() khi entity ĐÃ có id → Hibernate sinh SQL UPDATE
    // (khi id = null → sinh INSERT)
    // @UpdateTimestamp tự fill `updatedAt` mới
    Branch updated = branchRepository.save(existing);

    // === Bước 6: Trả về 200 OK + entity sau khi update ===
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBranchById(@PathVariable Long id) {
    // === Bước 2: Nếu không tồn tại → trả 404 và dừng ===
    if (!branchRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    // === Bước 3: Nếu tồn tại xoá theo id ===
    branchRepository.deleteById(id);

    return ResponseEntity.noContent().build();
  }

}
