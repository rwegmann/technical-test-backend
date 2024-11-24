package com.playtomic.tests.wallet.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter
    private Long id;
    
    @JsonIgnore
    @JoinColumn(name = "walletId")
    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    private Wallet wallet;
    
    @Column(name = "timestamp", nullable = false)
    @CreationTimestamp
    private OffsetDateTime timestamp;
    
    @Column(name = "amount", nullable = false)
    @NonNull
    private BigDecimal amount;

    @Column(name = "description", length = 255, nullable = false)
    @NonNull
    private String description;

    @Column(name = "previousBalance", nullable = false)
    @NonNull
    private BigDecimal previousBalance;
    
    @Column(name = "newBalance", nullable = false)
    @NonNull
    private BigDecimal newBalance;

    public Transaction(@NonNull Wallet wallet, @NonNull BigDecimal amount, @NonNull String description,
                       @NonNull BigDecimal previousBalance, @NonNull BigDecimal newBalance) {
        this.wallet = wallet;
        this.amount = amount;
        this.description = description;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
    }
    
    @JsonProperty(access = Access.READ_ONLY)
    public Long getWalletId() {
        return (wallet != null) ? wallet.getId() : null;
    }
}
