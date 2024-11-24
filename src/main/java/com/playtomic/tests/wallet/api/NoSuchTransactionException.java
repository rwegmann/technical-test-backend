package com.playtomic.tests.wallet.api;

import org.springframework.http.HttpStatus;

import lombok.NonNull;

public class NoSuchTransactionException extends WalletsApiException {
    
    @NonNull
    private Long transactionId;
    
    public NoSuchTransactionException(@NonNull Long walletId, @NonNull Long transactionId) {
        super(HttpStatus.NOT_FOUND, walletId);
        this.transactionId = transactionId;
    }

    @Override
    public String getMessage() {
        return "Transaction not found";
    }
    
}
