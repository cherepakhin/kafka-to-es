package ru.domru.logger.kafka.receiver.config;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.domru.logger.kafka.receiver.service.ElasticSearchEventLogConsumer;
import ru.domru.logger.kafka.receiver.service.ElasticSearchService;
import ru.domru.logger.kafka.receiver.service.KafkaErrorHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Value("${kafka.host}")
    private String kafkaHost;

    @Value("${kafka.namegroup}")
    private String nameGroup;

    @Value("${kafka.topic}")
    private String topic;

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.event_doc_type}")
    private String elasticsearchDocType;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, Map> consumerFactory() {
        final JsonDeserializer jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("*");
        ErrorHandlingDeserializer errorHandlingDeserializer
                = new ErrorHandlingDeserializer<>(jsonDeserializer);
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                errorHandlingDeserializer);
    }

    @Bean
    public KafkaErrorHandler kafkaErrorHandler() {
        return new KafkaErrorHandler();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map> kafkaListenerContainerFactory(
            @Autowired KafkaErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Map> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setErrorHandler(kafkaErrorHandler);
        return factory;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() throws MalformedURLException {
        if (!elasticsearchHost.contains("http")) {
            elasticsearchHost = "http://" + elasticsearchHost;
        }
        URL url = new URL(elasticsearchHost);
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(url.getHost(), url.getPort(), url.getProtocol())));
    }

    @Bean
    public ElasticSearchService elasticSearchService(@Autowired RestHighLevelClient client) {
        return new ElasticSearchService(client);
    }

    @Bean
    public KafkaMessageListenerContainer<String, Map> elasticSearchEventLogConsumerContainer(
            @Autowired ElasticSearchService elasticSearchService,
            @Autowired ConsumerFactory<String, Map> consumerFactory,
            @Autowired KafkaErrorHandler kafkaErrorHandler) {
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setGroupId(nameGroup);
        ElasticSearchEventLogConsumer elasticSearchEventLogConsumer
                = new ElasticSearchEventLogConsumer(elasticSearchService, elasticsearchDocType);
        containerProps.setMessageListener(elasticSearchEventLogConsumer);
        KafkaMessageListenerContainer<String, Map> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.setErrorHandler(kafkaErrorHandler);
        return container;
    }
}