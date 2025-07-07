package com.eaglebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankAccountResponse(
        String accountId,
        String accountType,
        String sortCode,
        String accountNumber,
        BigDecimal balance,
        LocalDateTime createdTimestamp
) {}
