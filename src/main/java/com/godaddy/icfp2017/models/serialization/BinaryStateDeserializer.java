package com.godaddy.icfp2017.models.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.godaddy.icfp2017.models.State;
import net.jpountz.lz4.LZ4BlockInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BinaryStateDeserializer extends StdDeserializer<State> {
  public BinaryStateDeserializer() {
    super(State.class);
  }

  @Override
  public State deserialize(
      final JsonParser p,
      final DeserializationContext ctxt) throws IOException {

    final Kryo kryo = KryoFactory.createKryo();

    final byte[] binaryValue = p.getBinaryValue();

    try (
        ByteArrayInputStream bais = new ByteArrayInputStream(binaryValue);
        LZ4BlockInputStream lz4 = new LZ4BlockInputStream(bais)) {
      return kryo.readObject(new Input(lz4), State.class);
    }
  }
}
