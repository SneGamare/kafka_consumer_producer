package com.plutus.kotak.commonlibs.service;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import com.plutus.kotak.commonlibs.entity.TransactionEntity;
import java.util.List;

public interface TransactionService {
    String createTransaction(BankTransaction transaction);
    List<TransactionEntity> getAllTransactions();
    TransactionEntity getTransactionById(String transactionId);
    void validateTransaction(BankTransaction transaction);
} 