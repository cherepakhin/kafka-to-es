package ru.domru.logger.kafka.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaEsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaEsApplication.class, args);
    }

}
