package com.playtomic.tests.wallet.api;

import org.springframework.http.HttpStatus;

import lombok.NonNull;

public class NoSuchWalletException extends WalletsApiException {
    
    public NoSuchWalletException(@NonNull Long walletId) {
        super(HttpStatus.NOT_FOUND, walletId);
    }

    @Override
    public String getMessage() {
        return "Wallet not found";
    }
    
}
