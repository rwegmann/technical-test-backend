package com.playtomic.tests.wallet.api;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public abstract class WalletsApiException extends RuntimeException {

    private @NonNull HttpStatus httpStatus;
    
    private @NonNull Long walletId;

    public WalletsApiException(@NonNull HttpStatus httpStatus, @NonNull Long walletId, Throwable cause) {
        super(cause);
        this.httpStatus = httpStatus;
        this.walletId = walletId;
    }

}
