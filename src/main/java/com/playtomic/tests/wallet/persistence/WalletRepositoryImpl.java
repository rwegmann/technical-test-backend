package com.playtomic.tests.wallet.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.playtomic.tests.wallet.api.InsufficientFundsException;
import com.playtomic.tests.wallet.api.NoSuchTransactionException;
import com.playtomic.tests.wallet.api.NoSuchWalletException;
import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.NonNull;

@Service
@Transactional
public class WalletRepositoryImpl implements WalletRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Wallet create() {
        Wallet wallet = new Wallet();
        entityManager.persist(wallet);
        return wallet;
    }

    @Override
    public Wallet update(@NonNull Wallet wallet) {
        Assert.notNull(wallet.getId(), "ID of provided wallet is null");
        wallet = entityManager.merge(wallet);
        return wallet;
    }

    @Override
    public Optional<Wallet> findById(@NonNull Long walletId) {
        return findById(walletId, LockModeType.PESSIMISTIC_READ);
    }

    public Optional<Wallet> findById(@NonNull Long id, @NonNull LockModeType lockModeType) {
        Wallet wallet = entityManager.find(Wallet.class, id, lockModeType);
        return Optional.ofNullable(wallet);
    }
    
    @Override
    public Wallet getById(@NonNull Long walletId) throws NoSuchWalletException {
        return getById(walletId, LockModeType.PESSIMISTIC_READ);
    }
    
    @Override
    public Wallet getByIdForUpdate(@NonNull Long walletId) throws NoSuchWalletException {
        return getById(walletId, LockModeType.PESSIMISTIC_WRITE);
    }

    public Wallet getById(@NonNull Long walletId, @NonNull LockModeType lockModeType) throws NoSuchWalletException {
        Optional<Wallet> walletOpt = findById(walletId, lockModeType);
        return walletOpt.orElseThrow(() -> new NoSuchWalletException(walletId));
    }

    @Override
    public List<Wallet> findAll() {
        TypedQuery<Wallet> query = entityManager.createQuery("FROM Wallet", Wallet.class);
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        return query.getResultList();
    }

    @Override
    public void deleteById(@NonNull Long id) throws NoSuchWalletException {
        Wallet wallet = getById(id, LockModeType.PESSIMISTIC_WRITE);
        entityManager.remove(wallet);
    }

    @Override
    public Optional<Transaction> findWalletTransactionById(@NonNull Long transactionId) {
        Transaction transaction = entityManager.find(Transaction.class, transactionId);
        return Optional.ofNullable(transaction);
    }

    @Override
    public Transaction getWalletTransactionById(@NonNull Long transactionId) throws NoSuchTransactionException {
        Optional<Transaction> transactionOpt = findWalletTransactionById(transactionId);
        return transactionOpt.orElseThrow(() -> new NoSuchTransactionException(null, transactionId)); //FIXME
    }

    @Override
    public List<Transaction> findAllWalletTransactions(@NonNull Long walletId) throws NoSuchWalletException {
        Wallet wallet = getById(walletId, LockModeType.PESSIMISTIC_READ);
        
        TypedQuery<Transaction> query = entityManager.createQuery("FROM Transaction WHERE wallet = :wallet", Transaction.class);
        query.setParameter("wallet", wallet);
        return query.getResultList();
    }

    @Override
    public Transaction addWalletTransaction(@NonNull Long walletId, @NonNull BigDecimal amount, @NonNull String description) throws NoSuchWalletException, InsufficientFundsException {
        Wallet wallet = getById(walletId, LockModeType.PESSIMISTIC_WRITE);
        
        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(wallet.getId(), currentBalance, amount);
        }
        wallet.setBalance(newBalance);
        Transaction transaction = new Transaction(wallet, amount, description, currentBalance, newBalance);
        entityManager.persist(transaction);
        return transaction;
    }

}
