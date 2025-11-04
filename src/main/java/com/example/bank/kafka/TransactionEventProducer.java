package com.example.bank.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(String eventJson) {
        kafkaTemplate.send("transaction-events", eventJson);
        log.info("Sent to Kafka: {}", eventJson);
    }
}
