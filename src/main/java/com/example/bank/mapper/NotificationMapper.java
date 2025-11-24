package com.example.bank.mapper;

import com.example.bank.Enums.OperationType;
import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.kafka.EventDTO;
import com.example.bank.Enums.NotflicationType;
import com.example.bank.model.Notification;
import com.example.bank.model.NotificationResponse;
import com.example.bank.model.Transaction.Transaction;
import com.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    private static AccountRepository accountRepository;

    public NotificationMapper(AccountRepository accountRepository) {
        NotificationMapper.accountRepository = accountRepository;
    }


    public static NotflicationType toNotificationType(OperationType opType) {
        switch (opType) {
            case deposit:
                return NotflicationType.DEPOSIT;
            case withdraw:
                return NotflicationType.WITHDRAW;
            case transfer:
                return NotflicationType.TRANSFER;
            case payment:
                return NotflicationType.PAYMENT;
            default:
                return NotflicationType.INFO; // fallback, если вдруг появится новый тип
        }
    }
    public static EventDTO toEventDTO(Transaction transaction) {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setType(transaction.getType());

        //Если мы депозитим значит счёт с которым проводится операция пользавталем to сли перевод снять from
        if (transaction.getType() == OperationType.deposit) {
            eventDTO.setAccountNumber(transaction.getToAccount());
        } else {
            eventDTO.setAccountNumber(transaction.getFromAccount());
        }

        eventDTO.setAccountTransferTo(transaction.getToAccount());
        eventDTO.setAmount(transaction.getAmount());
        eventDTO.setUserId(transaction.getUser().getUserId());
        eventDTO.setComment(transaction.getComment());
        eventDTO.setTransactionId(transaction.getId());
        return eventDTO;
    }
    public static Notification toNotification(EventDTO eventDTO) {
        Notification notification = new Notification();
        NotflicationType type = toNotificationType(eventDTO.getType());
        notification.setUserId(eventDTO.getUserId());
        notification.setType(type);
        if (type == NotflicationType.TRANSFER) {
            String AccountTransferTo = eventDTO.getAccountTransferTo();
            notification.setAccountTransferTo(AccountTransferTo);
        }
        notification.setAccountNumber(eventDTO.getAccountNumber());
        notification.setAmount(eventDTO.getAmount());
        notification.setComment(eventDTO.getComment());
        notification.setTitle(toTitle(type));
        notification.setReferenceId(eventDTO.getTransactionId());
        notification.setMessage(toMessage(eventDTO, type));

        return notification;
    }

    public static Notification makeNotificationForReciever(Notification notification) {
        Notification clone = new Notification();
        String accTrTo = notification.getAccountTransferTo();
        clone.setUserId(accountRepository.findByAccountNumber(accTrTo)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "UserId",accTrTo))
                .getUser().getUserId());
        clone.setType(notification.getType());
        clone.setTitle(notification.getTitle());
        clone.setAccountNumber(notification.getAccountNumber());
        clone.setAccountTransferTo(accTrTo);
        clone.setAmount(notification.getAmount());
        clone.setComment(notification.getComment());
        clone.setMessage(notification.getMessage());
        clone.setCreatedAt(notification.getCreatedAt());// новые всегда непрочитанные
        clone.setReferenceId(notification.getReferenceId());
        // ... и другие нужные поля
        return clone;
    }

    public static NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setAccountTransferTo(notification.getAccountTransferTo());
        // Маскируем номер только для TRANSFER
        if (notification.getType() == NotflicationType.TRANSFER) {
            response.setAccountNumber(maskAccount(notification.getAccountNumber()));
        } else {
            response.setAccountNumber(notification.getAccountNumber());
        }
        response.setComment(notification.getComment());
        response.setMessage(notification.getMessage());
        response.setRead(notification.getRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setAmount(notification.getAmount());
        response.setReferenceId(notification.getReferenceId());
        return response;
    }


    public static String toTitle(NotflicationType type) {
        switch (type) {
            case DEPOSIT:
                return "Зачисление";
            case WITHDRAW:
                return "Снятие";
            case TRANSFER:
                return "Перевод";
            case FRAUD:
                return "Подозрительная операция";
            case INFO:
                return "Информация";
            default:
                return "";
        }
    }
    public static String toMessage(EventDTO eventDTO, NotflicationType type) {
        String accountPart = "счёт: " + maskAccount(eventDTO.getAccountNumber());
        String amountPart = (eventDTO.getAmount() != null ? " на сумму " + eventDTO.getAmount() + " ₽" : "");
        String commentPart = (eventDTO.getComment() != null && !eventDTO.getComment().isEmpty())
                ? " (Комментарий: " + eventDTO.getComment() + ")"
                : "";


        switch (type) {
            case DEPOSIT:
                return "Пополнение " + accountPart + amountPart + commentPart;
            case WITHDRAW:
                return "Снятие средств с " + accountPart + amountPart + commentPart;
            case TRANSFER:
                String accountTransferToPart = "на счёт " + maskAccount(eventDTO.getAccountTransferTo());
                return "Перевод с счёта " + accountPart + amountPart + accountTransferToPart + commentPart;
            case FRAUD:
                return "Обнаружена подозрительная активность по вашему счёту. Срочно свяжитесь с банком!" + commentPart;
            case INFO:
            default:
                return commentPart.isEmpty() ? "Информационное уведомление" : commentPart;
        }
    }

    // Можно добавить маску для отображения номера счёта (****7071)
    private static String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber == null ? "" : accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
