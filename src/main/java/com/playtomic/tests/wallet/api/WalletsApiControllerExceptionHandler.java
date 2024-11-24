package com.playtomic.tests.wallet.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WalletsApiControllerExceptionHandler {
    
    private Logger log = LoggerFactory.getLogger(WalletsApiController.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        log.error(message, ex);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    @ExceptionHandler(NoSuchWalletException.class)
    public ResponseEntity<String> handleException(NoSuchWalletException ex) {
        String message = ex.getMessage();
        log.error(message, ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(message);
    }

    @ExceptionHandler(WalletsApiException.class)
    public ResponseEntity<String> handleException(WalletsApiException ex) {
        String message = ex.getMessage();
        log.error(message, ex);

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(message);
    }

}