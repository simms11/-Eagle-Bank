package com.eaglebank.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @NotBlank
    private String line1;

    private String line2;

    private String line3;

    @NotBlank
    private String town;

    @NotBlank
    private String county;

    @NotBlank
    private String postcode;
}
