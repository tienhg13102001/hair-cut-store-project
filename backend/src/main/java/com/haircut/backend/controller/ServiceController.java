package com.haircut.backend.controller;

import com.haircut.backend.entity.Service;
import com.haircut.backend.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/services")
public class ServiceController {

  private final ServiceRepository serviceRepository;

  public ServiceController(ServiceRepository serviceRepository) {
    this.serviceRepository = serviceRepository;
  }

  // === GET /services ===
  @GetMapping
  public List<Service> getAllServices() {
    return serviceRepository.findAll();
  }

  // === GET /services/{id} ===
  @GetMapping("/{id}")
  public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
    return serviceRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // === POST /services ===
  @PostMapping
  public ResponseEntity<Service> createService(@RequestBody Service service) {
    Service saved = serviceRepository.save(service);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  // === PUT /services/{id} ===
  @PutMapping("/{id}")
  public ResponseEntity<Service> updateService(
      @PathVariable Long id,
      @RequestBody Service input) {

    Optional<Service> result = serviceRepository.findById(id);
    if (result.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Service existing = result.get();
    existing.setName(input.getName());
    existing.setDescription(input.getDescription());
    existing.setDurationMin(input.getDurationMin());
    existing.setActive(input.isActive());

    Service updated = serviceRepository.save(existing);
    return ResponseEntity.ok(updated);
  }

  // === DELETE /services/{id} ===
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteService(@PathVariable Long id) {
    if (!serviceRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    serviceRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
