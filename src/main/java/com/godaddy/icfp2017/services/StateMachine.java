package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.godaddy.icfp2017.models.GameEndServerToPlayer;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandShakeP2S;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.ICFPMessage;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;

import java.io.IOException;
import java.util.function.Function;

final class StateMachine {
  interface Handler<T> {
    void capture(final ICFPMessage message);
    void debug(String s);
    SetupP2S setup(final SetupS2P message);
    GameplayP2S gameplay(final GameplayS2P message);
    T timeout();
    T stop(final GameEndServerToPlayer message);
  }

  private static final ObjectMapper MAPPER = JsonMapper.Instance;

  private final FrameReader input;
  private final FrameWriter output;

  StateMachine(
      final FrameReader inputStream,
      final FrameWriter outputStream) throws IOException {
    this.input = inputStream;
    this.output = outputStream;
  }

  private void send(final Object message) throws IOException {
    output.accept(MAPPER.writeValueAsBytes(message));
  }

  <T> T handshake(
      final HandShakeP2S message,
      final Function<HandshakeS2P,
      Handler<T>> handler) throws IOException {
    send(message);

    if (!input.hasNext()) {
      throw new IllegalStateException();
    }

    final HandshakeS2P s2p = MAPPER.readValue(input.next(), HandshakeS2P.class);
    return gameplay(handler.apply(s2p));
  }

  private <T> T gameplay(final Handler<T> handler) throws IOException {
    while (input.hasNext()) {
      final ObjectNode jsonNode = (ObjectNode) MAPPER.readTree(input.next());
      if (jsonNode.has("move")) {
        final GameplayS2P serverToPunter = MAPPER.treeToValue(jsonNode, GameplayS2P.class);
        send(handler.gameplay(serverToPunter));
        handler.capture(serverToPunter);
      } else if (
          jsonNode.has("punter") &&
          jsonNode.has("punters") &&
          jsonNode.has("map")) {
        final SetupS2P serverToPunter = MAPPER.readValue(input.next(), SetupS2P.class);
        send(handler.setup(serverToPunter));
        handler.capture(serverToPunter);
      } else if (jsonNode.has("timeout")) {
        return handler.timeout();
      } else if (jsonNode.has("stop")) {
        final GameEndServerToPlayer serverToPlayer = MAPPER.treeToValue(jsonNode, GameEndServerToPlayer.class);
        handler.capture(serverToPlayer);
        return handler.stop(serverToPlayer);
      } else {
        handler.debug("Unknown message received: " + jsonNode.toString());
      }
    }

    throw new IllegalStateException("Terminal message not received");
  }
}
