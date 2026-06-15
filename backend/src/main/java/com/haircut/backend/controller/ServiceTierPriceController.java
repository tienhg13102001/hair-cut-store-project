package com.haircut.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.CreateServiceTierPriceRequest;
import com.haircut.backend.dto.UpdateServiceTierPriceRequest;
import com.haircut.backend.entity.Service;
import com.haircut.backend.entity.ServiceTierPrice;
import com.haircut.backend.repository.ServiceRepository;
import com.haircut.backend.repository.ServiceTierPriceRepository;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/service-tier-prices")
public class ServiceTierPriceController {
  private final ServiceTierPriceRepository serviceTierPriceRepository;
  private final ServiceRepository serviceRepository;

  public ServiceTierPriceController(ServiceTierPriceRepository serviceTierPriceRepository,
      ServiceRepository serviceRepository) {
    this.serviceTierPriceRepository = serviceTierPriceRepository;
    this.serviceRepository = serviceRepository;
  }

  @GetMapping()
  public ResponseEntity<List<ServiceTierPrice>> getAllServiceTierPrices() {
    List<ServiceTierPrice> serviceTierPrices = serviceTierPriceRepository.findAll();
    return ResponseEntity.status(HttpStatus.OK).body(serviceTierPrices);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ServiceTierPrice> getServiceTierPrice(@PathVariable Long id) {
    Optional<ServiceTierPrice> serviceTierPrice = serviceTierPriceRepository.findById(id);
    if (serviceTierPrice.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.status(HttpStatus.OK).body(serviceTierPrice.get());
  }

  @GetMapping("/by-service/{serviceId}")
  public ResponseEntity<List<ServiceTierPrice>> getServiceTierPriceByServiceId(@PathVariable Long serviceId) {
    List<ServiceTierPrice> serviceTierPrice = serviceTierPriceRepository.findByServiceId(serviceId);
    return ResponseEntity.status(HttpStatus.OK).body(serviceTierPrice);
  }

  @PostMapping()
  public ResponseEntity<ServiceTierPrice> createServiceTierPrice(@RequestBody CreateServiceTierPriceRequest req) {
    Optional<Service> service = serviceRepository.findById(req.serviceId());
    if (service.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    boolean existServiceTierPrice = serviceTierPriceRepository.existsByServiceIdAndTier(
        req.serviceId(),
        req.tier());

    if (existServiceTierPrice) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    ServiceTierPrice data = new ServiceTierPrice();
    data.setPrice(req.price());
    data.setService(service.get());
    data.setTier(req.tier());

    ServiceTierPrice savedData = serviceTierPriceRepository.save(data);

    return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ServiceTierPrice> updateServiceTierPrice(@PathVariable Long id,
      @RequestBody UpdateServiceTierPriceRequest req) {
    Optional<ServiceTierPrice> result = serviceTierPriceRepository.findById(id);

    if (result.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    ServiceTierPrice serviceTierPriceData = result.get();

    serviceTierPriceData.setPrice(req.price());

    ServiceTierPrice savedServiceTierPrice = serviceTierPriceRepository.save(serviceTierPriceData);

    return ResponseEntity.status(HttpStatus.OK).body(savedServiceTierPrice);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteServiceTierPriceById(@PathVariable Long id) {
    if (!serviceTierPriceRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    serviceTierPriceRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
