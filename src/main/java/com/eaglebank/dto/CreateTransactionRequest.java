package com.eaglebank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotNull(message = "Sender account ID is required")
        String fromAccountId,

        @NotNull(message = "Receiver account ID is required")
        String toAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount
) {}
