package ru.domru.logger.kafka.receiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);
    RestHighLevelClient client;
    ObjectMapper objectMapper = new ObjectMapper();

    public ElasticSearchService(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Отправка в elastic search пакета событий
     *
     * @param indexName индекс в elasticsearch
     * @param event событие
     */
    public void bulk(String indexName, Map event) {
        try {
            client.bulk(createBulkRequest(indexName, event), RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("При отправке в elasticsearch возникла ошибка", e);
        }
    }

    BulkRequest createBulkRequest(String index, Map event) {
        BulkRequest bulkRequest = new BulkRequest();

        try {
            String json = objectMapper.writeValueAsString(event);
            IndexRequest rec = new IndexRequest(index).source(json, XContentType.JSON);
            bulkRequest.add(rec);
        } catch (JsonProcessingException e) {
            LOGGER.error("При подготовке пакета для elasticsearch возникла ошибка конвертирования в json", e);
        }
        return bulkRequest;
    }
}
