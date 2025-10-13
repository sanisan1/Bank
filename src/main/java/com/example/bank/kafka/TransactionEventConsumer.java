package com.example.bank.kafka;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionEventConsumer {

    @KafkaListener(topics = "TransactionEvents", groupId = "bank-notification-group")
    public void listen(String eventJson) {
        log.info("Received from Kafka: {}", eventJson);
    }
}
