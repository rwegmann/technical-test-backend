package com.playtomic.tests.wallet.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletPurchaseRequest;
import com.playtomic.tests.wallet.model.WalletTransactionResponse;
import com.playtomic.tests.wallet.model.WalletTopUpRequest;
import com.playtomic.tests.wallet.persistence.WalletRepository;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Transactional
public class WalletsApiController implements WalletsApi {
    private Logger log = LoggerFactory.getLogger(WalletsApiController.class);

    @NonNull
    private final StripeService stripeService;

    @NonNull
    private final WalletRepository walletRepository;
    
    @Override
    public ResponseEntity<Wallet> createWallet() {
        Wallet wallet = walletRepository.create();
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteWallet(Long walletId) throws NoSuchWalletException {
        walletRepository.deleteById(walletId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Transaction>> getAllWalletTransactions(Long walletId) throws NoSuchWalletException {
        List<Transaction> transactions = walletRepository.findAllWalletTransactions(walletId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        return new ResponseEntity<>(wallets, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Transaction> getTransactionById(Long walletId, Long transactionId) throws NoSuchTransactionException {
        Optional<Transaction> optional = walletRepository.findWalletTransactionById(transactionId);
        Transaction transaction = optional.orElse(null);
        if ((transaction == null) || !transaction.getWalletId().equals(walletId) ) {
            throw new NoSuchTransactionException(walletId, transactionId);
        }
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Wallet> getWalletById(Long walletId) throws NoSuchWalletException {
        Wallet wallet= walletRepository.getById(walletId);
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WalletTransactionResponse> topUpWallet(Long walletId, WalletTopUpRequest request) throws NoSuchWalletException, CreditCardTransactionException {
        // ensure wallet exists 
        Wallet wallet = walletRepository.getByIdForUpdate(walletId);
        
        // charge credit card
        @NonNull String card = request.getCreditCard();
        @NonNull BigDecimal amount = request.getAmount();
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be positive");
        try {
            stripeService.charge(card, amount);
        }
        catch (StripeAmountTooSmallException ex) {
            throw new CreditCardAmountTooSmallException(walletId, ex);
        }
        catch (StripeServiceException ex) {
            throw new CreditCardTransactionException(walletId, ex);
        }
        
        // register transaction
        return addWalletTransaction(wallet.getId(), request.getAmount(), request.getCreditCard());
    }

    @Override
    public ResponseEntity<WalletTransactionResponse> purchaseWithWallet(Long walletId, @Valid WalletPurchaseRequest request) throws NoSuchWalletException, InsufficientFundsException {
        BigDecimal amount = request.getAmount().negate();
        String description = request.getDescription();
        return addWalletTransaction(walletId, amount, description);
    }
    
    public ResponseEntity<WalletTransactionResponse> addWalletTransaction(Long walletId, BigDecimal amount, String description) throws NoSuchWalletException, InsufficientFundsException {
        Transaction transaction = walletRepository.addWalletTransaction(walletId, amount, description);
        Wallet wallet = walletRepository.getById(walletId);
        return new ResponseEntity<>(new WalletTransactionResponse(wallet, transaction), HttpStatus.OK);
    }
}
