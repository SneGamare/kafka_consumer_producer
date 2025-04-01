package com.plutus.kotak.commonlibs.service.impl;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import com.plutus.kotak.commonlibs.entity.TransactionEntity;
import com.plutus.kotak.commonlibs.repository.TransactionRepository;
import com.plutus.kotak.commonlibs.service.TransactionProducerService;
import com.plutus.kotak.commonlibs.service.TransactionService;
import com.plutus.kotak.commonlibs.exception.TransactionAlreadyExistsException;
import com.plutus.kotak.commonlibs.exception.TransactionNotFoundException;
import com.plutus.kotak.commonlibs.enums.TransactionStatus;
import com.plutus.kotak.commonlibs.enums.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionProducerService producerService;

    @Override
    @Transactional
    public String createTransaction(BankTransaction transaction) {
        log.debug("Creating new transaction: {}", transaction);
        
        // Validate transaction
        validateTransaction(transaction);

        // Check for existing transaction
        String transactionId = transaction.getTransactionId().toString();
        if (transactionRepository.existsById(transactionId)) {
            log.error("Transaction with ID {} already exists", transactionId);
            throw new TransactionAlreadyExistsException(
                String.format("Transaction with ID %s already exists", transactionId)
            );
        }

        // Convert and save transaction
        TransactionEntity entity = convertToEntity(transaction);
        TransactionEntity savedEntity = transactionRepository.save(entity);
        log.info("Successfully saved transaction: {}", savedEntity);

        // Produce Kafka message
        try {
            producerService.sendTransaction(transaction);
            log.info("Successfully produced transaction message to Kafka");
        } catch (Exception e) {
            log.error("Failed to produce transaction message to Kafka", e);
            // Consider if you want to roll back the transaction here
            // throw new KafkaProducerException("Failed to produce message to Kafka", e);
        }

        return savedEntity.getTransactionId();
    }

    @Override
    public List<TransactionEntity> getAllTransactions() {
        log.debug("Fetching all transactions");
        List<TransactionEntity> transactions = transactionRepository.findAll();
        log.info("Found {} transactions", transactions.size());
        return transactions;
    }

    @Override
    public TransactionEntity getTransactionById(String transactionId) {
        log.debug("Fetching transaction with ID: {}", transactionId);
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(
                String.format("Transaction with ID %s not found", transactionId)
            ));
    }

    @Override
    public void validateTransaction(BankTransaction transaction) {
        log.debug("Validating transaction: {}", transaction);
        
        if (Objects.isNull(transaction)) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (!StringUtils.hasText(transaction.getTransactionId().toString())) {
            throw new IllegalArgumentException("Transaction ID cannot be empty");
        }

        if (!StringUtils.hasText(transaction.getAccountId().toString())) {
            throw new IllegalArgumentException("Account ID cannot be empty");
        }

        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try {
            TransactionType.valueOf(transaction.getTransactionType().toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type");
        }

        try {
            TransactionStatus.valueOf(transaction.getStatus().toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction status");
        }
    }

    private TransactionEntity convertToEntity(BankTransaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(transaction.getTransactionId().toString());
        entity.setAccountId(transaction.getAccountId().toString());
        entity.setAmount(transaction.getAmount());
        entity.setTransactionType(transaction.getTransactionType().toString());
        entity.setTimestamp(transaction.getTimestamp());
        entity.setStatus(transaction.getStatus().toString());
        return entity;
    }
} 