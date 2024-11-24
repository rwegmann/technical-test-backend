package com.playtomic.tests.wallet.service.impl;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.service.Payment;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;

/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */
@RestClientTest(StripeService.class)
@ActiveProfiles(profiles = "test")
public class StripeServiceTest {

    @Value("${stripe.simulator.charges-uri}")
    private String stripeServiceChargesUri;
    
    @Value("${stripe.simulator.refunds-uri}")
    private String stripeServiceRefundsUri;
    
    @Autowired
    private StripeService stripeService;

    @Autowired
    private MockRestServiceServer server;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void test_exception() {
        server.reset();
        server.expect(requestTo(stripeServiceChargesUri))
            .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));
        
        Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
            stripeService.charge("4242 4242 4242 4242", new BigDecimal(5));
        });
    }

    @Test
    public void test_ok() throws StripeServiceException, JsonProcessingException {
        server.reset();
        Payment result = new Payment("payment-id-1");
        server.expect(requestTo(stripeServiceChargesUri))
            .andRespond(withSuccess(objectMapper.writeValueAsBytes(result), MediaType.APPLICATION_JSON));
        
        Payment payment = stripeService.charge("4242 4242 4242 4242", new BigDecimal(15));
        Assertions.assertEquals(payment.getId(), result.getId());
    }
}
