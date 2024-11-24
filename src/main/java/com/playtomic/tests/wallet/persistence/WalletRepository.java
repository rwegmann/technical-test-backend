package com.playtomic.tests.wallet.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.playtomic.tests.wallet.api.InsufficientFundsException;
import com.playtomic.tests.wallet.api.NoSuchTransactionException;
import com.playtomic.tests.wallet.api.NoSuchWalletException;
import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;

import lombok.NonNull;

public interface WalletRepository {

    public Wallet create();

    public Wallet update(@NonNull Wallet wallet);
    
    public Optional<Wallet> findById(@NonNull Long walletId);

    public Wallet getById(@NonNull Long walletId) throws NoSuchWalletException;
    
    public Wallet getByIdForUpdate(@NonNull Long walletId) throws NoSuchWalletException;

    public List<Wallet> findAll();

    public void deleteById(@NonNull Long walletId) throws NoSuchWalletException;

    public Optional<Transaction> findWalletTransactionById(@NonNull Long transactionId);
    
    public Transaction getWalletTransactionById(@NonNull Long transactionId) throws NoSuchTransactionException;

    public List<Transaction> findAllWalletTransactions(@NonNull Long walletId) throws NoSuchWalletException;

    public Transaction addWalletTransaction(@NonNull Long walletId, @NonNull BigDecimal amount, @NonNull String description) throws NoSuchWalletException, InsufficientFundsException;

}
