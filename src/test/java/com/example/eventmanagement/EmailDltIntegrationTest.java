package com.example.eventmanagement;

import com.example.eventmanagement.emaillog.EmailRepository;
import com.example.eventmanagement.notification.EmailEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import java.time.Duration;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {"email-events", "email-events.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class EmailDltIntegrationTest {

    @Autowired
    private KafkaTemplate<String, EmailEvent> template;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @MockitoBean
    private EmailRepository emailRepository;

    @Test
    void failedEventGoesToDlt() {

        template.send("email-events", new EmailEvent("fail@test.com", "Hi", "Body"));

        Map<String, Object> props =
                KafkaTestUtils.consumerProps(broker, "test-dlt-verifier", false);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

        try (Consumer<String, EmailEvent> consumer = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(EmailEvent.class, false)).createConsumer()) {
            broker.consumeFromAnEmbeddedTopic(consumer, "email-events.DLT");

            ConsumerRecord<String, EmailEvent> record =
                    KafkaTestUtils.getSingleRecord(consumer, "email-events.DLT", Duration.ofSeconds(30));

            assertNotNull(record);
            assertEquals("fail@test.com", record.value().to());
        }
    }
}