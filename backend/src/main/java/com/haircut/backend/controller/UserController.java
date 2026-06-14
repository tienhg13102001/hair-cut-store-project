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
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> result = userRepository.findAll();

    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @PostMapping()
  public ResponseEntity<User> createUser(@RequestBody User user) {
    User userSaved = userRepository.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(userSaved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    Optional<User> findedUser = userRepository.findById(id);
    if (findedUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
    return ResponseEntity.status(HttpStatus.OK).body(findedUser.get());
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUserById(@PathVariable Long id, @RequestBody User user) {
    Optional<User> responseData = userRepository.findById(id);
    if (responseData.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    User existingUser = responseData.get();

    existingUser.setEmail(user.getEmail());
    existingUser.setFullName(user.getFullName());
    existingUser.setPhone(user.getPhone());
    existingUser.setRole(user.getRole());
    existingUser.setAvatarUrl(user.getAvatarUrl());
    existingUser.setGuest(user.isGuest());
    existingUser.setActive(user.isActive());

    User updatedUser = userRepository.save(existingUser);

    return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
    Optional<User> responseData = userRepository.findById(id);
    if (responseData.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    userRepository.deleteById(id);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
