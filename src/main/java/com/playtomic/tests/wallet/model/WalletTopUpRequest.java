package com.playtomic.tests.wallet.model;

import java.math.BigDecimal;

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
public class WalletTopUpRequest {
    
    @NonNull
    private String creditCard;
    
    @NonNull
    private BigDecimal amount;
}
