package com.playtomic.tests.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class WalletTransactionResponse {

    @NonNull
    private Wallet wallet;

    @NonNull
    private Transaction transaction;

}
