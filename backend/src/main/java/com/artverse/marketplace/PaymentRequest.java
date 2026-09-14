package com.artverse.marketplace;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(@NotBlank String paymentReference) {}
