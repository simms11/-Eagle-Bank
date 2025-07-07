package com.eaglebank.service;

import com.eaglebank.dto.AddressRequest;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.entity.Address;
import com.eaglebank.entity.User;
import com.eaglebank.exception.ConflictException;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest request;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        AddressRequest address = new AddressRequest("123 Main St", "Apt 1", "", "London", "Greater London", "W1A 1AA");

        request = new CreateUserRequest(
                "Jane Smith",
                "+447111222333",
                "jane.smith@example.com",
                address,
                "secret456"
        );

        user = User.builder()
                .id(userId)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("+447111222333")
                .password("hashedPassword")
                .address(Address.builder()
                        .line1("123 Main St")
                        .line2("Apt 1")
                        .town("London")
                        .county("Greater London")
                        .postcode("W1A 1AA")
                        .build())
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
    }


    @Test
    void testCreateUser_conflictEmailAlreadyExists() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUserById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getName(), response.name());
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void testGetUserById_withAuth_success() {
        when(authentication.getName()).thenReturn("jane.smith@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId, authentication);

        assertNotNull(response);
        assertEquals("Jane Smith", response.name());
    }

    @Test
    void testGetUserById_withAuth_forbiddenAccess() {
        when(authentication.getName()).thenReturn("unauthorized@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> userService.getUserById(userId, authentication));
    }

    @Test
    void testGetUserById_withAuth_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId, authentication));
    }
}
