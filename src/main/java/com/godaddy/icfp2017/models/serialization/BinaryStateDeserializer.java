package com.godaddy.icfp2017.models.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.godaddy.icfp2017.models.State;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BinaryStateDeserializer extends StdDeserializer<State> {
  public BinaryStateDeserializer() {
    super(State.class);
  }

  @Override
  public State deserialize(
      final JsonParser p,
      final DeserializationContext ctxt) throws IOException, JsonProcessingException {

    final Kryo kryo = KryoFactory.createKryo();

    final byte[] binaryValue = p.getBinaryValue();

    return kryo.readObject(new Input(new ByteArrayInputStream(binaryValue)), State.class);
  }
}
