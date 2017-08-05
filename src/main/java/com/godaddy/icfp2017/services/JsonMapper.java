package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class JsonMapper extends ObjectMapper {
  public static final JsonMapper Instance = new JsonMapper();

  public JsonMapper() {
    registerModule(new GuavaModule());
    registerModule(new SimpleModule() {
      {
        addSerializer(SimpleWeightedGraph.class, new JsonSerializer<SimpleWeightedGraph>() {
          @Override
          public void serialize(
              final SimpleWeightedGraph value,
              final JsonGenerator gen,
              final SerializerProvider serializers)
              throws IOException {

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
              objectOutputStream.writeObject(value);
            }

            gen.writeBinary(byteArrayOutputStream.toByteArray());
          }
        });

        addDeserializer(SimpleWeightedGraph.class, new JsonDeserializer<SimpleWeightedGraph>() {
          @Override
          public SimpleWeightedGraph deserialize(
              final JsonParser p,
              final DeserializationContext ctxt)
              throws IOException, JsonProcessingException {
            final byte[] binaryValue = p.getBinaryValue();
            try (final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream
                                                                                       (binaryValue))) {
              try {
                return (SimpleWeightedGraph) objectInputStream.readObject();
              }
              catch (ClassNotFoundException e) {
                return null;
              }
            }
          }
        });
      }

    });
  }
}
