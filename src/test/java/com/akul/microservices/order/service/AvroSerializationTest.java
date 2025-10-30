package com.akul.microservices.order.service;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class AvroSerializationTest {

    @Test
    void testSerializeOrder() {
        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", "http://localhost:8085");

        KafkaAvroSerializer serializer = new KafkaAvroSerializer();
        serializer.configure(config, false);

        String schemaString = "{\n" +
                              "  \"type\": \"record\",\n" +
                              "  \"name\": \"OrderPlaced\",\n" +
                              "  \"namespace\": \"com.example\",\n" +
                              "  \"fields\": [\n" +
                              "    {\"name\": \"skuCode\", \"type\": \"string\"},\n" +
                              "    {\"name\": \"price\", \"type\": \"double\"},\n" +
                              "    {\"name\": \"quantity\", \"type\": \"int\"},\n" +
                              "    {\"name\": \"userDetails\", \"type\": {\n" +
                              "      \"type\": \"record\",\n" +
                              "      \"name\": \"UserDetails\",\n" +
                              "      \"fields\": [\n" +
                              "        {\"name\": \"email\", \"type\": \"string\"},\n" +
                              "        {\"name\": \"firstName\", \"type\": \"string\"},\n" +
                              "        {\"name\": \"lastName\", \"type\": \"string\"}\n" +
                              "      ]\n" +
                              "    }}\n" +
                              "  ]\n" +
                              "}";

        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(schemaString);

        GenericRecord userDetails = new GenericData.Record(schema.getField("userDetails").schema());
        userDetails.put("email", "andrii@example.com");
        userDetails.put("firstName", "Andrii");
        userDetails.put("lastName", "K");

        GenericRecord order = new GenericData.Record(schema);
        order.put("skuCode", "iphone_15");
        order.put("price", 1350.0);
        order.put("quantity", 3);
        order.put("userDetails", userDetails);

        byte[] data = serializer.serialize("order-placed-value", order);
        System.out.println("Serialization successful, bytes length: " + data.length);
    }
}
