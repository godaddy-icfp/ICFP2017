package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.godaddy.icfp2017.models.GameEndServerToPlayer;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandShakeP2S;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.ICFPMessage;
import com.godaddy.icfp2017.models.ServerToPlayer;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.TimeoutServerToPlayer;

import java.io.IOException;
import java.util.function.Function;

public final class StateMachine {
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
      final Function<HandshakeS2P, Handler<T>> handler) throws IOException {
    send(message);

    if (!input.hasNext()) {
      throw new IllegalStateException();
    }

    final HandshakeS2P s2p = MAPPER.readValue(input.next(), HandshakeS2P.class);
    return setup(handler.apply(s2p));
  }

  private <T> T setup(final Handler<T> handler) throws IOException {
    if (!input.hasNext()) {
      throw new IllegalStateException();
    }

    final SetupS2P setupS2P = MAPPER.readValue(input.next(), SetupS2P.class);
    send(handler.setup(setupS2P));
    handler.capture(setupS2P);
    return gameplay(setupS2P.getMap().getSites().size(), handler);
  }

  private <T> T gameplay(final int movesRemaining, final Handler<T> handler) throws IOException {
    for (int i = movesRemaining; i >= 0 && input.hasNext(); --i) {
      final ObjectNode jsonNode = (ObjectNode) MAPPER.readTree(input.next());
      if (jsonNode.has("move")) {
        final GameplayS2P serverToPlayer = MAPPER.treeToValue(jsonNode, GameplayS2P.class);
        final GameplayP2S message = handler.gameplay(serverToPlayer);
        send(message);
        handler.capture(serverToPlayer);
      } else if (jsonNode.has("timeout")) {
        return handler.timeout();
      } else if (jsonNode.has("stop")) {
        final GameEndServerToPlayer serverToPlayer = MAPPER.treeToValue(jsonNode, GameEndServerToPlayer.class);
        handler.capture(serverToPlayer);
        return handler.stop(serverToPlayer);
      }
    }

    throw new IllegalStateException();
  }

  interface Handler<T> {
    void capture(final ICFPMessage message);
    SetupP2S setup(final SetupS2P message);
    GameplayP2S gameplay(final GameplayS2P message);
    T timeout();
    T stop(final GameEndServerToPlayer message);
  }
}
