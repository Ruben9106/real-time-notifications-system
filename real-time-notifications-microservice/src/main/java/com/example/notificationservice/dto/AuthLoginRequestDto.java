package com.example.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequestDto {
    @NotBlank
    String username;
    @NotBlank
    String password;
}
