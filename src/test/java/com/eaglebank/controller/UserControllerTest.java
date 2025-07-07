package com.eaglebank.controller;

import com.eaglebank.dto.AddressRequest;
import com.eaglebank.dto.AddressResponse;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    private CreateUserRequest request;
    private UserResponse expectedResponse;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);

        AddressRequest address = new AddressRequest(
                "123 Main Street", "Building A", "", "London", "Greater London", "W1A 1AA"
        );

        request = new CreateUserRequest(
                "Alsahid Simms",
                "+447123456789",
                "alsahid.simms@example.com",
                address,
                "secure123"
        );

        expectedResponse = new UserResponse(
                "usr-" + UUID.randomUUID(),
                request.name(),
                request.email(),
                request.phoneNumber(),
                new AddressResponse(
                        address.line1(),
                        address.line2(),
                        address.line3(),
                        address.town(),
                        address.county(),
                        address.postcode()
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void testCreateUser_returnsCreatedResponse() {
        // Arrange
        when(userService.createUser(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.createUser(request);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse.email(), response.getBody().email());
        assertEquals(expectedResponse.name(), response.getBody().name());
        verify(userService).createUser(request);
    }
}
