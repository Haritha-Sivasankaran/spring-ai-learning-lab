package com.example.studybuddy.dto;

public record UserResponse(
    String id,
    String email,
    String name,
    String token
) {}
