package com.example.bank.mapper;

import com.example.bank.model.Transaction.Transaction;
import com.example.bank.model.Transaction.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public static TransactionResponse toDto(Transaction transaction) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(transaction.getId());
        dto.setFromAccount(transaction.getFromAccount());
        dto.setToAccount(transaction.getToAccount());
        dto.setAmount(transaction.getAmount());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setType(transaction.getType());
        dto.setComment(transaction.getComment());
        return dto;
    }
}