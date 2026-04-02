package com.akul.microservices.order.service;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvroSerializationTest {

    @Test
    void testSerializeAndDeserializeOrderPlacedEvent() {

        MockSchemaRegistryClient schemaRegistry = new MockSchemaRegistryClient();

        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", "mock://test");

        KafkaAvroSerializer serializer = new KafkaAvroSerializer(schemaRegistry);
        serializer.configure(config, false);

        KafkaAvroDeserializer deserializer = new KafkaAvroDeserializer(schemaRegistry);
        deserializer.configure(config, false);

        String schemaString =
                """
                        {
                          "type": "record",
                          "name": "OrderPlacedEvent",
                          "namespace": "com.akul.microservices.order",
                          "fields": [
                            {"name": "orderNumber", "type": "string"},
                            {"name": "status", "type": "string"}
                          ]
                        }
                        """;

        Schema schema = new Schema.Parser().parse(schemaString);

        GenericRecord event = new GenericData.Record(schema);
        event.put("orderNumber", "ORD12345");
        event.put("status", "CREATED");

        byte[] bytes = serializer.serialize("order-created", event);

        Object result = deserializer.deserialize("order-created", bytes);

        GenericRecord record = (GenericRecord) result;

        assertEquals("ORD12345", record.get("orderNumber").toString());
        assertEquals("CREATED", record.get("status").toString());
    }
}
