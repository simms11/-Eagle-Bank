package com.eaglebank.controller;

import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.TransactionResponse;
import com.eaglebank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication
    ) {
        TransactionResponse response = transactionService.createTransaction(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(Authentication authentication) {
        List<TransactionResponse> transactions = transactionService.getTransactions(authentication);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID transactionId,
            Authentication auth
    ) {
        TransactionResponse response = transactionService.getTransactionById(transactionId, auth);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsForAccount(
            @PathVariable UUID accountId,
            Authentication auth
    ) {
        List<TransactionResponse> txns = transactionService.getTransactionsForAccount(accountId, auth);
        return ResponseEntity.ok(txns);
    }


}
