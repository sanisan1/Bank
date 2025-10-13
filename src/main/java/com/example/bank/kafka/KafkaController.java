package com.example.bank.kafka;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    private final ProducerService producer;

    public KafkaController(ProducerService producer) { this.producer = producer; }

    @PostMapping("/publish")
    public String publish(@RequestParam String message) {
        producer.send("test-topic", message);
        return "sent";
    }
}
