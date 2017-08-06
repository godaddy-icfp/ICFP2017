package com.godaddy.icfp2017.services;

import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

final class FrameReader implements AutoCloseable, Iterator<byte[]> {
  private final BufferedInputStream reader;
  private final Consumer<byte[]> debug;
  private byte[] next;

  public FrameReader(final InputStream reader, Consumer<byte[]> debug) {
    if (reader instanceof BufferedInputStream) {
      this.reader = ((BufferedInputStream) reader);
    } else {
      this.reader = new BufferedInputStream(reader);
    }

    this.debug = debug;
  }

  @Override
  public void close() throws Exception {
    reader.close();
  }

  @Override
  public boolean hasNext() {
    final StringBuilder sb = new StringBuilder(10);

    try {
      for (int i = 0; i < 10; ++i) {
        int ch = reader.read();
        if (ch == ':') {
          final int len = Integer.parseUnsignedInt(sb.toString(), 10);
          final byte[] buffer = new byte[len];
          ByteStreams.read(reader, buffer, 0, len);
          next = buffer;
          debug.accept(buffer);
          return true;
        } else {
          sb.append((char) ch);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return false;
  }

  @Override
  public byte[] next() {
    return next;
  }
}
