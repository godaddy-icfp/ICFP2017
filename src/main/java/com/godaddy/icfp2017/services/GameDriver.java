package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.godaddy.icfp2017.models.*;
import com.google.common.collect.Queues;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Queue;

public class GameDriver implements AutoCloseable {

  private final InputStream inputStream;
  private final PrintStream outputStream;
  private final PrintStream debugStream;

  private final GameLogic gameLogic;

  private final InboundMessageParser inboundMessageParser;

  private final Queue<ICFPMessage> capture = Queues.newConcurrentLinkedQueue();

  private final boolean shouldCapture;

  public GameDriver(
      final InputStream inputStream,
      final OutputStream outputStream,
      final OutputStream debugStream,
      final GameLogic gameLogic,
      final boolean shouldCapture) {
    this.inputStream = inputStream;
    this.outputStream = new PrintStream(outputStream);
    this.debugStream = new PrintStream(Optional.ofNullable(debugStream).orElseGet(ByteStreams::nullOutputStream));
    this.gameLogic = gameLogic;
    this.shouldCapture = shouldCapture;

    inboundMessageParser = new InboundMessageParser();
  }

  public void run(final String name) throws Exception {

    // send the handshake message to the server
    final HandShakeP2S handShakeP2S = new HandShakeP2S();
    handShakeP2S.setMe(name);
    sendMessage(handShakeP2S);

    // get the handshake from the server
    final Optional<ServerToPlayer> message = getMessage();

    if (!message.isPresent()) {
      return;
    }

    final HandshakeS2P handshakeS2P = (HandshakeS2P) message.get();

    while (true) {
      // read the next message from the server
      final Optional<ServerToPlayer> s2pOptional = getMessage();

      if (!s2pOptional.isPresent()) {
        debugStream.println("Got unknown input");
        continue;
      }

      final ServerToPlayer serverToPlayerMessage = s2pOptional.get();

      if (shouldCapture) {
        capture.add(serverToPlayerMessage);
      }

      // setup
      if (serverToPlayerMessage instanceof SetupS2P) {
        final SetupS2P setupS2P = (SetupS2P) serverToPlayerMessage;
        final SetupP2S setupResponse = gameLogic.setup(setupS2P);
        sendMessage(setupResponse);
        if (shouldCapture) {
          capture.add(setupResponse);
        }
      }

      // gameplay
      else if (serverToPlayerMessage instanceof GameplayS2P) {
        final GameplayS2P gameplayS2P = (GameplayS2P) serverToPlayerMessage;
        final GameplayP2S moveResponse = gameLogic.move(gameplayS2P, null);
        sendMessage(moveResponse);
        if (shouldCapture) {
          capture.add(moveResponse);
        }
      }
      else if (serverToPlayerMessage instanceof TimeoutServerToPlayer) {
        debugStream.println("Got a timeout message");
      }
      else if (serverToPlayerMessage instanceof GameEndServerToPlayer) {
        debugStream.println("game over");
        return;
      }
    }
  }

  @Override
  public void close() throws Exception {
    gameLogic.close();
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

  private void sendMessage(PlayerToServer p2s) throws JsonProcessingException {

    String json = JsonMapper.Instance.writeValueAsString(p2s);
    final String output = json.length() + 1 + ":" + json + '\n';
    outputStream.print(output);
    outputStream.flush();
    debugStream.println("out => " + output);
  }

  private Optional<ServerToPlayer> getMessage() throws Exception {
    final int messageLength = readInteger();
    if (messageLength == 0) {
      return Optional.empty();
    }

    final String messageString = readString(messageLength);

    debugStream.println("in => " + messageLength + ":" + messageString);

    if (messageString.length() > 0) {
      return inboundMessageParser.getNextMessage(messageString);
    }
    else {
      return Optional.empty();
    }
  }

  private int readInteger() throws IOException {
    StringBuilder sb = new StringBuilder();
    char c;
    do {
      c = (char) inputStream.read();

      if (Character.isDigit(c)) {
        sb.append(c);
      }
    } while (Character.isDigit(c) || Character.isWhitespace(c));
    if (sb.length() > 0) {
      return Integer.parseInt(sb.toString());
    }
    else {
      return 0;
    }
  }

  private String readString(final int length) throws IOException {
    final byte[] buffer = new byte[1024];
    int totalLengthRead = 0;
    StringBuilder sb = new StringBuilder();
    while (totalLengthRead < length) {
      int charsToRead = Math.min(buffer.length, length - totalLengthRead);
      final int bytesRead = inputStream.read(buffer, 0, charsToRead);
      totalLengthRead += bytesRead;
      sb.append(new String(buffer, 0, bytesRead));
    }
    return sb.toString();
  }
}
