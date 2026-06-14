package com.haircut.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.entity.User;
import com.haircut.backend.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping()
  public ResponseEntity<List<User>> getMethodName() {
    List<User> result = userRepository.findAll();
    if (result.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.status(HttpStatus.FOUND).body(result);
  }

}
