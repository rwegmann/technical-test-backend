/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.9.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletPurchaseRequest;
import com.playtomic.tests.wallet.model.WalletTopUpRequest;
import com.playtomic.tests.wallet.model.WalletTransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-11-23T10:02:26.168473700+01:00[Europe/Madrid]", comments = "Generator version: 7.9.0")
@Validated
public interface WalletsApi {

    /**
     * POST /wallets : Create a new wallet
     *
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/wallets",
        produces = { "application/json" }
    )
    
    ResponseEntity<Wallet> createWallet(
        
    );


    /**
     * DELETE /wallets/{walletId} : Delete a wallet
     *
     * @param walletId ID of wallet (required)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/wallets/{walletId}"
    )
    
    ResponseEntity<Void> deleteWallet(
         @PathVariable("walletId") Long walletId
    );


    /**
     * GET /wallets/{walletId}/transactions : Get all transactions of a wallet
     *
     * @param walletId ID of wallet (required)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/wallets/{walletId}/transactions",
        produces = { "application/json" }
    )
    
    ResponseEntity<List<Transaction>> getAllWalletTransactions(
         @PathVariable("walletId") Long walletId
    );


    /**
     * GET /wallets : Get all wallets
     *
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/wallets",
        produces = { "application/json" }
    )
    
    ResponseEntity<List<Wallet>> getAllWallets(
        
    );


    /**
     * GET /wallets/{walletId}/transactions/{transactionId} : Get a transaction by its ID
     *
     * @param walletId ID of wallet (required)
     * @param transactionId ID of a wallet transaction (required)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/wallets/{walletId}/transactions/{transactionId}",
        produces = { "application/json" }
    )
    
    ResponseEntity<Transaction> getTransactionById(
         @PathVariable("walletId") Long walletId,
         @PathVariable("transactionId") Long transactionId
    );


    /**
     * GET /wallets/{walletId} : Get a wallet by its ID
     *
     * @param walletId ID of wallet (required)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/wallets/{walletId}",
        produces = { "application/json" }
    )
    
    ResponseEntity<Wallet> getWalletById(
         @PathVariable("walletId") Long walletId
    );


    /**
     * POST /wallets/{walletId}/purchase : Purchase 
     *
     * @param walletId ID of wallet (required)
     * @param walletPurchaseRequest  (optional)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/wallets/{walletId}/purchase",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    ResponseEntity<WalletTransactionResponse> purchaseWithWallet(
         @PathVariable("walletId") Long walletId,
         @Valid @RequestBody(required = false) WalletPurchaseRequest walletPurchaseRequest
    );


    /**
     * POST /wallets/{walletId}/topUp : Top up a wallet
     *
     * @param walletId ID of wallet (required)
     * @param walletTopUpRequest  (optional)
     * @return successful operation (status code 200)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/wallets/{walletId}/topUp",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    ResponseEntity<WalletTransactionResponse> topUpWallet(
         @PathVariable("walletId") Long walletId,
         @Valid @RequestBody(required = false) WalletTopUpRequest walletTopUpRequest
    );

}