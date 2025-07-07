package com.eaglebank.service;

import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID userId);

    UserResponse getUserById(UUID userId, Authentication authentication);

    UserResponse updateUser(UUID userId, UpdateUserRequest request, Authentication authentication);

    void deleteUserById(UUID userId, Authentication authentication);

}
