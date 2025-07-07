package com.eaglebank.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        AddressResponse address,
        LocalDateTime createdTimestamp,
        LocalDateTime updatedTimestamp
) {}
