package com.eaglebank.service;

import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.dto.BankAccountResponse;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public interface BankAccountService {
    BankAccountResponse createAccount(CreateBankAccountRequest request, Authentication auth);

    List<BankAccountResponse> getAccounts(Authentication auth);

    BankAccountResponse getAccountById(UUID accountId, Authentication auth);

    BankAccountResponse updateAccount(UUID accountId, CreateBankAccountRequest request, Authentication auth);

    void deleteAccount(UUID accountId, Authentication auth);

    BankAccountResponse deposit(UUID accountId, BigDecimal amount, Authentication auth);
    BankAccountResponse withdraw(UUID accountId, BigDecimal amount, Authentication auth);

}

