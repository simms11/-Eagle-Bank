package com.eaglebank.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotNull String name,

        @Pattern(
                regexp = "^\\+[1-9]\\d{1,14}$",
                message = "Phone number must be in the following format (e.g. +447123456789)"
        )
        @NotBlank
        String phoneNumber,

        @Email(message = "Must be a valid email")
        @NotBlank
        String email,

        @Valid @NotNull
        AddressRequest address,

        @NotBlank
        String password
) {}
