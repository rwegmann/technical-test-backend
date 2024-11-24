package com.playtomic.tests.wallet.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class WalletTransactionResponse {

    @NonNull
    @NotNull
    private Wallet wallet;

    @NonNull
    @NotNull
    private Transaction transaction;

}
