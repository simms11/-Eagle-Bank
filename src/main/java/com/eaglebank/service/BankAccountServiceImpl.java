package com.eaglebank.service;

import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.entity.BankAccount;
import com.eaglebank.entity.User;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService{
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BankAccountResponse createAccount(CreateBankAccountRequest request, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount account = new BankAccount();
        account.setAccountType(request.accountType());
        account.setBankName(request.bankName());
        account.setSortCode(request.sortCode());
        account.setAccountNumber(request.accountNumber());
        account.setBalance(request.balance());
        account.setUser(user);
        account.setCreatedTimestamp(LocalDateTime.now());
        account.setUpdatedTimestamp(LocalDateTime.now());



        BankAccount saved = bankAccountRepository.save(account);

        return toResponse(saved);
    }

    @Override
    public List<BankAccountResponse> getAccounts(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bankAccountRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public BankAccountResponse getAccountById(UUID accountId, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or access denied"));

        return toResponse(account);
    }

    @Override
    @Transactional
    public BankAccountResponse updateAccount(UUID accountId, CreateBankAccountRequest request, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to update this account");
        }

        account.setAccountNumber(request.accountNumber());
        account.setAccountType(request.accountType());
        account.setBalance(request.balance());
        account.setSortCode(request.sortCode());
        account.setBankName(request.bankName());
        account.setUpdatedTimestamp(LocalDateTime.now());

        BankAccount updated = bankAccountRepository.save(account);

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID accountId, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to delete this account");
        }

        bankAccountRepository.delete(account);
    }

    @Override
    @Transactional
    public BankAccountResponse deposit(UUID accountId, BigDecimal amount, Authentication auth) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Ensure ownership
        String email = auth.getName();
        if (!account.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to deposit to this account");
        }

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedTimestamp(LocalDateTime.now());

        return toResponse(bankAccountRepository.save(account));
    }

    @Override
    @Transactional
    public BankAccountResponse withdraw(UUID accountId, BigDecimal amount, Authentication auth) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String email = auth.getName();
        if (!account.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You are not allowed to withdraw from this account");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedTimestamp(LocalDateTime.now());

        return toResponse(bankAccountRepository.save(account));
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return new BankAccountResponse(
                account.getId().toString(),
                account.getAccountType(),
                account.getSortCode(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedTimestamp()
        );
    }

}
