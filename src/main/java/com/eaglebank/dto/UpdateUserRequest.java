package com.eaglebank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber,
        AddressRequest address
) {}
