package ru.domru.logger.kafka.receiver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ElasticSearchEventLogConsumer implements MessageListener<String, Map> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    ObjectMapper mapper = new ObjectMapper();

    String docType;
    ElasticSearchService elasticSearchService;

    public ElasticSearchEventLogConsumer(ElasticSearchService elasticSearchService, String docType) {
        this.elasticSearchService = elasticSearchService;
        this.docType=docType;
    }

    @Override
    public void onMessage(ConsumerRecord<String, Map> record) {
        elasticSearchService.bulk(getIndexName(), docType, record.value());
    }

    String getIndexName() {
        LocalDate day = LocalDate.now();
        return String.format("device-%s", day.format(formatter));
    }
}
