package com.playtomic.tests.wallet.api;

import org.springframework.http.HttpStatus;

import lombok.NonNull;

public class CreditCardTransactionException extends WalletsApiException {

    public CreditCardTransactionException(@NonNull Long walletId, Throwable cause) {
        this(HttpStatus.BAD_GATEWAY, walletId, cause);
    }

    public CreditCardTransactionException(@NonNull HttpStatus httpsStatus, @NonNull Long walletId, Throwable cause) {
        super(httpsStatus, walletId, cause);
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder("Failed to charge credit card");
        Throwable cause = getCause();
        if (cause != null) {
            message.append(": ");
            message.append(cause.getMessage());
        }
        return message.toString();
    }
    
}