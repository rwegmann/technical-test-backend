package com.playtomic.tests.wallet.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletPurchaseRequest;
import com.playtomic.tests.wallet.model.WalletTopUpRequest;
import com.playtomic.tests.wallet.model.WalletTransactionResponse;
import com.playtomic.tests.wallet.persistence.WalletRepository;
import com.playtomic.tests.wallet.service.Payment;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
@Transactional
public class WalletsApiController implements WalletsApi {

    @NonNull
    private final StripeService stripeService;

    @NonNull
    private final WalletRepository walletRepository;
    
    @Override
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        return new ResponseEntity<>(wallets, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Wallet> createWallet() {
        Wallet wallet = walletRepository.create();
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Wallet> getWalletById(Long walletId) throws NoSuchWalletException {
        Assert.notNull(walletId, "walletId is null");
        Wallet wallet = walletRepository.getById(walletId);
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteWallet(Long walletId) throws NoSuchWalletException {
        Assert.notNull(walletId, "walletId is null");
        walletRepository.deleteById(walletId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WalletTransactionResponse> topUpWallet(Long walletId, @Valid WalletTopUpRequest request) throws NoSuchWalletException, CreditCardTransactionException, CreditCardAmountTooSmallException {
        String card = request.getCreditCard();
        BigDecimal amount = request.getAmount();
        
        // ensure wallet exists 
        Wallet wallet = walletRepository.getByIdForUpdate(walletId);
        
        try {
            // charge credit card
            Payment payment = stripeService.charge(card, amount);
            
            // register transaction
            String description = "Top Up: paymentId = " + payment.getId();
            return addWalletTransaction(wallet.getId(), request.getAmount(), description);
        }
        catch (StripeAmountTooSmallException ex) {
            throw new CreditCardAmountTooSmallException(walletId, ex);
        }
        catch (StripeServiceException ex) {
            throw new CreditCardTransactionException(walletId, ex);
        }
        
    }

    @Override
    public ResponseEntity<WalletTransactionResponse> purchaseWithWallet(Long walletId, @Valid WalletPurchaseRequest request) throws NoSuchWalletException, InsufficientFundsException {
        BigDecimal amount = request.getAmount().negate();
        String description = request.getDescription();
        return addWalletTransaction(walletId, amount, description);
    }

    @Override
    public ResponseEntity<List<Transaction>> getAllWalletTransactions(Long walletId) throws NoSuchWalletException {
        List<Transaction> transactions = walletRepository.findAllWalletTransactions(walletId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
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

    public ResponseEntity<WalletTransactionResponse> addWalletTransaction(Long walletId, BigDecimal amount, String description) throws NoSuchWalletException, InsufficientFundsException {
        Transaction transaction = walletRepository.addWalletTransaction(walletId, amount, description);
        Wallet wallet = walletRepository.getById(walletId);
        return new ResponseEntity<>(new WalletTransactionResponse(wallet, transaction), HttpStatus.OK);
    }
}
