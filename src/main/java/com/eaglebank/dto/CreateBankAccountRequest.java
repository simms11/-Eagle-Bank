package com.eaglebank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateBankAccountRequest(

        @NotBlank
        String accountType,
        @NotBlank String bankName,
        @NotBlank
        String sortCode,

        @NotBlank
        String accountNumber,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal balance
) {}
