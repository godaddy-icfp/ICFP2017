package com.godaddy.icfp2017.services;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

final class FrameWriter implements Consumer<byte[]> {
  private final BufferedOutputStream outputStream;
  private final Consumer<byte[]> debug;

  FrameWriter(final OutputStream outputStream, Consumer<byte[]> debug) {
    if (outputStream instanceof BufferedOutputStream) {
      this.outputStream = ((BufferedOutputStream) outputStream);
    } else {
      this.outputStream = new BufferedOutputStream(outputStream);
    }

    this.debug = debug;
  }

  @Override
  public void accept(final byte[] bytes) {
    debug.accept(bytes);

    final StringBuilder sb = new StringBuilder(10);
    sb.append(bytes.length);
    sb.append(':');

    try {
      outputStream.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
      outputStream.write(bytes);
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
