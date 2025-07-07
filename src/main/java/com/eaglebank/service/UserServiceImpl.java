package com.eaglebank.service;

import com.eaglebank.dto.AddressResponse;
import com.eaglebank.dto.CreateUserRequest;
import com.eaglebank.dto.UpdateUserRequest;
import com.eaglebank.dto.UserResponse;
import com.eaglebank.entity.Address;
import com.eaglebank.entity.User;
import com.eaglebank.exception.ConflictException;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final BankAccountRepository  bankAccountRepository;

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                toAddressResponse(user.getAddress()),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        Address address = Address.builder()
                .line1(request.address().line1())
                .build();

        User toSave = User.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .address(address)
                .build();

        // save and grab the persisted entity (now has ID + timestamps)
        User savedUser = userRepository.save(toSave);

        // return using savedUser, not the pre-save 'user'
        return new UserResponse(
                savedUser.getId().toString(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                toAddressResponse(savedUser.getAddress()),
                savedUser.getCreatedTimestamp(),
                savedUser.getUpdatedTimestamp()
        );
    }


    @Override
    public UserResponse getUserById(UUID userId, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //Extract email from the authenticated user
        String authEmail = authentication.getName(); //JWT sets this to the user's email

        //Ensure the authenticated user is accessing their own data
        if(!user.getEmail().equals(authEmail)) {
            throw new ForbiddenException("You are not authorised to access this user's information");
        }

        return new UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                toAddressResponse(user.getAddress()),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(authentication.getName())) {
            throw new ForbiddenException("You are not authorised to update this user's information");
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());

        Address address = user.getAddress();
        address.setLine1(request.address().line1());
        address.setLine2(request.address().line2());
        address.setLine3(request.address().line3());
        address.setTown(request.address().town());
        address.setCounty(request.address().county());
        address.setPostcode(request.address().postcode());

        user.setUpdatedTimestamp(LocalDateTime.now());
        userRepository.save(user);

        return new UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                toAddressResponse(user.getAddress()),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }

    @Override
    @Transactional
    public void deleteUserById(UUID userId, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Ensure the authenticated user is deleting themselves
        if (!user.getEmail().equals(authentication.getName())) {
            throw new ForbiddenException("You are not authorised to delete this user");
        }

        // Check if user has a bank account
        boolean hasAccount = bankAccountRepository.existsByUserId(userId);
        if (hasAccount) {
            throw new ConflictException("User has a bank account and cannot be deleted");
        }

        userRepository.delete(user);
    }


    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getLine1(),
                address.getLine2(),
                address.getLine3(),
                address.getTown(),
                address.getCounty(),
                address.getPostcode()
        );
    }
}
