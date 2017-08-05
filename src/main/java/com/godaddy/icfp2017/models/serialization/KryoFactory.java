package com.godaddy.icfp2017.models.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.objenesis.strategy.StdInstantiatorStrategy;

class KryoFactory {
  private KryoFactory() {
  }

  static Kryo createKryo() {
    final Kryo kryo = new Kryo();
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    kryo.register(SimpleWeightedGraph.class, new JavaSerializer());
    ImmutableListSerializer.registerSerializers(kryo);
    ImmutableSetSerializer.registerSerializers(kryo);
    ImmutableMapSerializer.registerSerializers(kryo);

    return kryo;
  }
}

