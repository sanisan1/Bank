package com.example.bank.mapper;

import com.example.bank.model.Transaction.Transfers;
import com.example.bank.model.Transaction.TransferResponseDto;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public static TransferResponseDto toDto(Transfers transfer) {
        if (transfer == null) {
            return null;
        }

        TransferResponseDto dto = new TransferResponseDto();
        dto.setId(transfer.getId());
        dto.setAmount(transfer.getAmount());
        dto.setOperationType(transfer.getOperationType().name());
        dto.setTime(transfer.getTime());

        if (transfer.getFromAccount() != null) {
            dto.setFromAccountId(transfer.getFromAccount().getId());
        }
        if (transfer.getFromUser() != null) {
            dto.setFromUserId(transfer.getFromUser().getUserId());
        }
        if (transfer.getToAccount() != null) {
            dto.setToAccountId(transfer.getToAccount().getId());
        }
        if (transfer.getToUser() != null) {
            dto.setToUserId(transfer.getToUser().getUserId());
        }

        dto.setComment(transfer.getComment());
        return dto;
    }
}
