package com.eaglebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String transactionId,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        LocalDateTime createdTimestamp
) {}
