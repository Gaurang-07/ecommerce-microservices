package com.ecommerce.notification.config;

import com.ecommerce.shared.events.OrderCreatedEvent;
import com.ecommerce.shared.events.PaymentProcessedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ─────────────────────────────────────────
    // Base consumer properties
    // Shared between all consumer factories
    // ─────────────────────────────────────────
    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class);
        return props;
    }

    // ─────────────────────────────────────────
    // Consumer Factory for OrderCreatedEvent
    // ─────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();

        // JsonDeserializer typed to OrderCreatedEvent
        // Converts Kafka bytes → OrderCreatedEvent object
        JsonDeserializer<OrderCreatedEvent> deserializer =
            new JsonDeserializer<>(OrderCreatedEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            deserializer
        );
    }

    // Container factory for @KafkaListener
    // This is what containerFactory = "orderKafkaListenerContainerFactory" references
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
            orderKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
            factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderConsumerFactory());
        return factory;
    }

    // ─────────────────────────────────────────
    // Consumer Factory for PaymentProcessedEvent
    // ─────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, PaymentProcessedEvent> paymentConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();

        JsonDeserializer<PaymentProcessedEvent> deserializer =
            new JsonDeserializer<>(PaymentProcessedEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent>
            paymentKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent>
            factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentConsumerFactory());
        return factory;
    }
}