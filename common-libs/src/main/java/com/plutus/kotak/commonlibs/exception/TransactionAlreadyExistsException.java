package com.plutus.kotak.commonlibs.exception;

public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String message) {
        super(message);
    }
} 