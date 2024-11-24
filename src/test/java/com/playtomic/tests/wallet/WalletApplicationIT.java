package com.playtomic.tests.wallet;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.model.WalletPurchaseRequest;
import com.playtomic.tests.wallet.model.WalletTopUpRequest;
import com.playtomic.tests.wallet.service.Payment;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc 
public class WalletApplicationIT {
    
    private static final String CREDIT_CARD = "1234 5678 1234 5678";

    private static final BigDecimal AMOUNT_ZERO = BigDecimal.ZERO;
    
    private static final BigDecimal AMOUNT_ONE = BigDecimal.ONE;
    
    private static final BigDecimal AMOUNT_TEN = BigDecimal.valueOf(10);
    
    private static final BigDecimal AMOUNT_TWENTY = BigDecimal.valueOf(20);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        Mockito.when(stripeService.charge(CREDIT_CARD, AMOUNT_ONE))
            .thenThrow(new StripeAmountTooSmallException());
        
        Mockito.when(stripeService.charge(CREDIT_CARD, AMOUNT_TEN))
            .thenReturn(new Payment(AMOUNT_TEN.toString()));
        
        Mockito.when(stripeService.charge(CREDIT_CARD, AMOUNT_TWENTY))
            .thenThrow(new StripeServiceException());
    }

    @Test
    public void whenValidInput_thenCreateWallet() throws Exception {
        mvc.perform(post("/api/v1/wallets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    public void whenValidInput_thenGetAllWallets() throws Exception {
        mvc.perform(post("/api/v1/wallets")).andReturn();
        mvc.perform(post("/api/v1/wallets")).andReturn();
        
        mvc.perform(get("/api/v1/wallets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    public void whenValidInput_thenGetWallet() throws Exception {
        Long walletId = createWallet();
        
        WalletTopUpRequest request = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TEN);
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
             .andExpect(status().isOk());
        
        mvc.perform(get("/api/v1/wallets/" + walletId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(walletId))
            .andExpect(jsonPath("$.balance").value(AMOUNT_TEN.doubleValue()));
    }

    @Test
    public void whenValidInput_thenDeleteWallet() throws Exception {
        Long walletId = createWallet();
        
        mvc.perform(get("/api/v1/wallets/" + walletId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(walletId));
        
        mvc.perform(delete("/api/v1/wallets/" + walletId))
            .andExpect(status().isOk());
        
        mvc.perform(get("/api/v1/wallets/" + walletId))
            .andExpect(status().isNotFound());
    }

    @Test
    public void whenValidInput_thenTopUpWallet() throws Exception {
        Long walletId = createWallet();
        
        WalletTopUpRequest request = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TEN);
        
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wallet.id").value(walletId))
            .andExpect(jsonPath("$.wallet.balance").value(AMOUNT_TEN.doubleValue()))
            .andExpect(jsonPath("$.transaction.amount").value(AMOUNT_TEN.doubleValue()));
    }

    @Test
    public void whenInvalidInput_thenTopUpWalletFails() throws Exception {
        Long walletId = createWallet();
        
        WalletTopUpRequest request = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TEN.negate());
        
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void whenValidInput_thenTopUpWalletWithTooSmallAmount() throws Exception {
        Long walletId = createWallet();
        
        WalletTopUpRequest request = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_ONE);
        
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void whenStripeServiceFails_thenTopUpWalletFails() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/wallets")).andReturn();
        String walletId = String.valueOf((int)JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
        
        WalletTopUpRequest request = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TWENTY);
        
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadGateway());
    }

    @Test
    public void whenValidInput_thenPurchaseWithWallet() throws Exception {
        Long walletId = createWallet();
        
        WalletTopUpRequest topUpRequest = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TEN);
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(topUpRequest)))
            .andExpect(status().isOk());

        WalletPurchaseRequest request = new WalletPurchaseRequest(AMOUNT_TEN, "description");
        mvc.perform(post("/api/v1/wallets/" + walletId + "/purchase").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wallet.id").value(walletId))
            .andExpect(jsonPath("$.wallet.balance").value(AMOUNT_ZERO.doubleValue()))
            .andExpect(jsonPath("$.transaction.amount").value(-AMOUNT_TEN.doubleValue()));
    }

    @Test
    public void whenInsufficientFunds_thenPurchaseWithWallet() throws Exception {
        Long walletId = createWallet();

        WalletPurchaseRequest request = new WalletPurchaseRequest(AMOUNT_TEN, "description");
        mvc.perform(post("/api/v1/wallets/" + walletId + "/purchase").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isPaymentRequired());
    }

    @Test
    public void testConcurreny() throws Exception {
        Long walletId = createWallet();

        final int concurrentRequestCount = 1000;
        final int concurrentThreadCount = 20;
        
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreadCount);
        Callable<Void> callableTask = () -> {
            concurrentExecutionMethod(walletId);
            return null;
        };
        
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentRequestCount; i++) {
            Future<Void> future = executorService.submit(callableTask);
            futures.add(future);
        }
        
        for (Future<Void> f : futures) {
            f.get();
        }
        
        // check that the final balance is zero. The idea being that if concurrency issues 
        // exist, the final balance will be incorrect
        mvc.perform(get("/api/v1/wallets/" + walletId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(walletId))
            .andExpect(jsonPath("$.balance").value(AMOUNT_ZERO.doubleValue()));
    }

    public void concurrentExecutionMethod(Long walletId) throws Exception {
        // top up with 10
        WalletTopUpRequest topUpRequest = new WalletTopUpRequest(CREDIT_CARD, AMOUNT_TEN);
        mvc.perform(post("/api/v1/wallets/" + walletId + "/topUp").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(topUpRequest)))
            .andExpect(status().isOk());

        // purchase 10 times with 1
        for (int i = 0; i < 10; i++) {
            WalletPurchaseRequest request = new WalletPurchaseRequest(AMOUNT_ONE, "description");
            mvc.perform(post("/api/v1/wallets/" + walletId + "/purchase").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());
        }
    }
    
    protected Long createWallet() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/wallets"))
           .andExpect(status().isOk())
           .andReturn();
        Wallet wallet = objectMapper.readValue(result.getResponse().getContentAsString(), Wallet.class);
        return wallet.getId();
    }
    
    // FIXME: test transactions API
    
}
