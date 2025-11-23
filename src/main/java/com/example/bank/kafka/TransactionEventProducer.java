package com.example.bank.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;


    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }



    public void sendTransactionEvent(EventDTO eventDTO) {
        String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(eventDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при сериализации события", e);
        }
        kafkaTemplate.send("TransactionEvent", eventJson);

        log.info("Sent to Kafka: {}", eventJson);
    }


}
