package com.example.mediaservice.dto;

public record UserDto(
        String email,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String avatarUrl,
        String occupation
) {
}
