package com.haircut.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.haircut.backend.dto.CreateGreetingRequest;
import com.haircut.backend.dto.Greeting;

@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Xin chào Spring Boot!";
  }

  @GetMapping("/hello/{name}")
  public String getMethodName(@PathVariable String name) {
    return "Xin chao " + name + "!";
  }

  @GetMapping("/greet")
  public Greeting greet(@RequestParam String name, @RequestParam(defaultValue = "10") int age) {
    return new Greeting("Hello", name, age);
  }

  @PostMapping("/greet")
  public ResponseEntity<Greeting> createGreeting(@RequestBody CreateGreetingRequest request) {
    Greeting greeting = new Greeting("Hello", request.name(), request.age());
    return ResponseEntity.status(HttpStatus.CREATED).body(greeting);
  }

}