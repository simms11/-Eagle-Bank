package com.eaglebank.dto;

public record AddressResponse(
        String line1,
        String line2,
        String line3,
        String town,
        String county,
        String postcode
) {}