package com.eaglebank.controller;

import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId,
                                                    Authentication authentication) {
        UserResponse user = userService.getUserById(userId, authentication);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        UUID uuid = UUID.fromString(userId.replace("usr-", ""));
        UserResponse response = userService.updateUser(uuid, request, authentication);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String userId,
            Authentication authentication) throws BadRequestException {

        try {
            UUID uuid = UUID.fromString(userId.replace("usr-", ""));
            userService.deleteUserById(uuid, authentication);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid user ID format");
        }
    }


}



