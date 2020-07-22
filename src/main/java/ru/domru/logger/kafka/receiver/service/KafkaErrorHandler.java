package ru.domru.logger.kafka.receiver.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.DeserializationException;

public class KafkaErrorHandler implements org.springframework.kafka.listener.ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaErrorHandler.class);

    @Override
    public void handle(Exception e, ConsumerRecord<?, ?> consumerRecord) {
        LOGGER.info("Error mapping json {} {}", e, consumerRecord);
    }

    @Override
    public void handle(Exception e, ConsumerRecord<?, ?> data, Consumer<?, ?> consumer) {
        if (e instanceof DeserializationException) {
            DeserializationException exception = (DeserializationException) e;
            String dataStr = new String(exception.getData());
            LOGGER.error("Error mapping json {}", dataStr);
        } else {
            LOGGER.error(e.getMessage());
        }
    }
}
