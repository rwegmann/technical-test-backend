package com.playtomic.tests.wallet.api;

import org.springframework.http.HttpStatus;

import lombok.NonNull;

public class CreditCardAmountTooSmallException extends CreditCardTransactionException {

    public CreditCardAmountTooSmallException(@NonNull Long walletId, Throwable cause) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, walletId, cause);
    }

    @Override
    public String getMessage() {
        return "Amount is too small to bee charged by credit card";
    }
    
}