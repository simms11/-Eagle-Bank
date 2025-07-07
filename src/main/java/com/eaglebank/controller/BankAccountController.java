package com.eaglebank.controller;

import com.eaglebank.dto.BankAccountResponse;
import com.eaglebank.dto.CreateBankAccountRequest;
import com.eaglebank.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody CreateBankAccountRequest request,
            Authentication auth
    ) {
        BankAccountResponse response = bankAccountService.createAccount(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAccounts(Authentication auth) {
        return ResponseEntity.ok(bankAccountService.getAccounts(auth));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountResponse> getAccountById(
            @PathVariable UUID accountId,
            Authentication auth
    ) {
        BankAccountResponse response = bankAccountService.getAccountById(accountId, auth);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<BankAccountResponse> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateBankAccountRequest request,
            Authentication auth
    ) {
        BankAccountResponse updated = bankAccountService.updateAccount(accountId, request, auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId,
            Authentication auth
    ) {
        bankAccountService.deleteAccount(accountId, auth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<BankAccountResponse> deposit(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            Authentication auth
    ) {
        BankAccountResponse response = bankAccountService.deposit(id, amount, auth);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            Authentication auth
    ) {
        BankAccountResponse response = bankAccountService.withdraw(id, amount, auth);
        return ResponseEntity.ok(response);
    }

}
