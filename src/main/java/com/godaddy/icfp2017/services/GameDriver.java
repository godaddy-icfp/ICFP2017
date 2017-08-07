package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.godaddy.icfp2017.models.*;
import com.google.common.collect.Queues;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class GameDriver {

  private final StateMachine stateMachine;
  private final PrintStream debugStream;
  private final GameLogic gameLogic;

  private final Queue<ICFPMessage> capture = Queues.newConcurrentLinkedQueue();
  private final boolean shouldCapture;

  public GameDriver(
      final InputStream inputStream,
      final OutputStream outputStream,
      final PrintStream debugStream,
      final GameLogic gameLogic,
      final boolean shouldCapture) throws IOException {
    this.debugStream = debugStream;
    this.stateMachine = new StateMachine(
        new FrameReader(inputStream, buffer -> this.debug("in => ", buffer)),
        new FrameWriter(outputStream, buffer -> this.debug("out => ", buffer)));

    this.gameLogic = gameLogic;
    this.shouldCapture = shouldCapture;
  }

  private void debug(final String prefix, final byte[] bytes) {
    debugStream.print(prefix);
    debugStream.println(new String(bytes, StandardCharsets.US_ASCII));
    debugStream.flush();
  }

  private static HandShakeP2S getHandshake(final String name) {
    final HandShakeP2S handShakeP2S = new HandShakeP2S();
    handShakeP2S.setMe(name);
    return handShakeP2S;
  }

  public void run(final String name) throws Exception {

    // send the handshake message to the server
    stateMachine.handshake(getHandshake(name), handshakeS2P -> new StateMachine.Handler<Void>() {
      @Override
      public void capture(final ICFPMessage message) {
        if (shouldCapture) {
          capture.add(message);
        }
      }

      @Override
      public void debug(final String s) {
        debugStream.println(s);
      }

      @Override
      public SetupP2S setup(final SetupS2P message) {
        return gameLogic.setup(message);
      }

      @Override
      public GameplayP2S gameplay(final GameplayS2P message) {
        return gameLogic.move(message);
      }

      @Override
      public void timeout() {
        debugStream.println("Got a timeout message");
      }

      @Override
      public Void stop(final GameEndServerToPlayer message) {
        debugStream.println("game over");
        return null;
      }
    });
  }

  public void dumpCapture(final PrintStream output) {

    output.println("------- CAPTURE (FOR REPLAY) -------");
    output.println();

    for (final ICFPMessage icfpMessage : capture) {

      if (icfpMessage instanceof GameplayP2S) {
        ((GameplayP2S) icfpMessage).setState(null);
      }

      try {
        output.println(JsonMapper.Instance.writeValueAsString(icfpMessage));
      }
      catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }
}
