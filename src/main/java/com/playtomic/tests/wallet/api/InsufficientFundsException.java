package com.playtomic.tests.wallet.api;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class InsufficientFundsException extends WalletsApiException {

    @NonNull
    private BigDecimal currentBalance;
    
    @NonNull 
    private BigDecimal requestedAmount;
    
    public InsufficientFundsException(@NonNull Long walletId, @NonNull BigDecimal currentBalance, @NonNull BigDecimal requestedAmount) {
        super(HttpStatus.PAYMENT_REQUIRED, walletId);
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    @Override
    public String getMessage() {
        return "Wallet has insufficient funds: current balance = " + currentBalance + ", requested amount: " + requestedAmount;
    }
    
}
