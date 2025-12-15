package com.akul.microservices.order.service;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AvroSerializationTest {

    @Test
    void testSerializeOrderPlacedEvent() throws Exception {
        MockSchemaRegistryClient schemaRegistry = new MockSchemaRegistryClient();

        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", "mock://test");

        KafkaAvroSerializer serializer = new KafkaAvroSerializer(schemaRegistry);
        serializer.configure(config, false);


        String schemaString = "{\n" +
                              "  \"type\": \"record\",\n" +
                              "  \"name\": \"OrderPlacedEvent\",\n" +
                              "  \"namespace\": \"com.akul.microservices.order.infrastructure.messaging.kafka.avro\",\n" +
                              "  \"fields\": [\n" +
                              "    {\"name\": \"orderNumber\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"email\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"firstName\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"lastName\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"status\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"items\", \"type\": {\"type\": \"array\", \"items\": {\"type\": \"record\", \"name\": \"OrderItem\", \"fields\": [\n" +
                              "      {\"name\": \"sku\", \"type\": \"string\"},\n" +
                              "      {\"name\": \"price\", \"type\": \"double\"},\n" +
                              "      {\"name\": \"quantity\", \"type\": \"int\"}\n" +
                              "    ]}}}\n" +
                              "  ]\n" +
                              "}";

        Schema schema = new Schema.Parser().parse(schemaString);
        schemaRegistry.register("com.akul.microservices.order.infrastructure.messaging.kafka.avro.OrderPlacedEvent", schema);


        Schema orderItemSchema = schema.getField("items").schema().getElementType();
        GenericRecord item1 = new GenericData.Record(orderItemSchema);
        item1.put("sku", "iphone_15");
        item1.put("price", 1350.0);
        item1.put("quantity", 2);

        GenericRecord item2 = new GenericData.Record(orderItemSchema);
        item2.put("sku", "airpods_pro");
        item2.put("price", 250.0);
        item2.put("quantity", 1);

        GenericRecord orderEvent = new GenericData.Record(schema);
        orderEvent.put("orderNumber", "ORD12345");
        orderEvent.put("email", "andrii@example.com");
        orderEvent.put("firstName", "Andrii");
        orderEvent.put("lastName", "K");
        orderEvent.put("status", "CREATED");
        orderEvent.put("items", Arrays.asList(item1, item2));


        byte[] data = serializer.serialize("order-placed-value", orderEvent);
        System.out.println("Serialization successful, bytes length: " + data.length);
    }
}
