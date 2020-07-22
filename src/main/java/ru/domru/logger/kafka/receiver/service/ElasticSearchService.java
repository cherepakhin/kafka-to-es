package ru.domru.logger.kafka.receiver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);
    private final BulkProcessor bulkProcessor;
    RestHighLevelClient client;
    ObjectMapper objectMapper = new ObjectMapper();

    public ElasticSearchService(RestHighLevelClient client,
                                int countDocs,
                                int sizeQueueMBs,
                                int flushIntervatSeconds,
                                int countConcurentRequests) {
        this.client = client;
        this.bulkProcessor = createBulkProcessor(
                client,
                countDocs,
                sizeQueueMBs,
                flushIntervatSeconds,
                countConcurentRequests);
    }

    BulkProcessor createBulkProcessor(RestHighLevelClient client,
                                      int countDocs,
                                      int sizeQueueMBs,
                                      int flushIntervatSeconds,
                                      int countConcurentRequests
    ) {
        return BulkProcessor.builder(
                (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        LOGGER.info("BEFORE going to execute bulk of {} requests", request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        LOGGER.info("AFTER bulk executed {} failures", response.hasFailures() ? "with" : "without");
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        LOGGER.warn("AFTER error while executing bulk", failure);
                    }
                })
                .setBulkActions(countDocs)
                .setBulkSize(new ByteSizeValue(sizeQueueMBs, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(flushIntervatSeconds))
                .setConcurrentRequests(countConcurentRequests)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }

    /**
     * Отправка в elastic search пакета событий
     *
     * @param indexName индекс в elasticsearch
     * @param event     событие
     */
    public void bulk(String indexName, Map event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            IndexRequest rec = new IndexRequest(indexName).source(json, XContentType.JSON);
            bulkProcessor.add(rec);
        } catch (JsonProcessingException e) {
            LOGGER.error("При подготовке пакета для elasticsearch возникла ошибка конвертирования в json", e);
        }
    }

}
