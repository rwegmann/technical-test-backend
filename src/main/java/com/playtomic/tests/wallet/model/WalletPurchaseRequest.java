package com.playtomic.tests.wallet.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class WalletPurchaseRequest {

    @NonNull
    @NotNull
    @Positive
    private BigDecimal amount;

    @NonNull
    @NotEmpty
    private String description;

}
