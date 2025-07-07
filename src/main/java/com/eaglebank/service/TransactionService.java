package com.eaglebank.service;

import com.eaglebank.dto.CreateTransactionRequest;
import com.eaglebank.dto.TransactionResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request, Authentication auth);

    List<TransactionResponse> getTransactions(Authentication authentication);

    TransactionResponse getTransactionById(UUID transactionId, Authentication auth);

    List<TransactionResponse> getTransactionsForAccount(UUID accountId, Authentication auth);
}
