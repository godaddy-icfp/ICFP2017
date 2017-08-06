package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandShakeP2S;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StateMachineTests {
  private static class DummyException extends RuntimeException {
  }

  private static InputStream prefix(long l) {
    final String str = Long.toString(l, 10) + ':';
    return new ByteArrayInputStream(str.getBytes(StandardCharsets.US_ASCII));
  }

  private static InputStream framedResource(final String resourceName) {
    final ByteSource byteSource = Resources.asByteSource(Resources
        .getResource(resourceName));

    try {
      return new SequenceInputStream(
        prefix(byteSource.size()),
        byteSource.openStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static StateMachine createStateMachine(
      final String resource,
      final String... resources) throws IOException {
    final InputStream stream = Stream
        .of(resources)
        .map(StateMachineTests::framedResource)
        .reduce(framedResource(resource), SequenceInputStream::new);

    return new StateMachine(
        new FrameReader(stream, msg -> {}),
        new FrameWriter(ByteStreams.nullOutputStream(), msg -> {}));
  }

  @Test
  public void handshakeTest() throws Exception {
    try (InputStream handshake = framedResource("HandshakeS2P.json")) {
      final StateMachine sm = new StateMachine(
          new FrameReader(handshake, msg -> {}),
          new FrameWriter(ByteStreams.nullOutputStream(), msg -> {}));
      try {
        sm.handshake(new HandShakeP2S(), serverToPlayer -> {
          assertThat(serverToPlayer.getYou()).isEqualTo("GoDaddy");
          throw new DummyException();
        });

        throw new IllegalStateException();
      } catch (DummyException e) {
        // do nothing
      }
    }
  }

  @Test
  public void canDeserializeSetup() throws Exception {
    try (final StateMachine sm = createStateMachine(
        "HandshakeS2P.json",
        "SetupS2P.json")) {
      try {
        sm.handshake(new HandShakeP2S(), serverToPlayer -> {
          assertThat(serverToPlayer.getYou()).isEqualTo("GoDaddy");
          return new TestHandler() {
            @Override
            public SetupP2S setup(final SetupS2P message) {
              assertThat(message.getPunter()).isEqualTo(0);
              throw new DummyException();
            }
          };
        });

        throw new IllegalStateException();
      } catch (DummyException e) {
        // do nothing
      }
    }
  }


  @Test
  public void canDeserializeOnlineGameplay() throws Exception {
    try (final StateMachine sm = createStateMachine(
        "HandshakeS2P.json",
        "SetupS2P.json",
        "GameplayS2P.json")) {
      try {
        boolean[] receivedSetup = new boolean[]{false};
        sm.handshake(new HandShakeP2S(), serverToPlayer -> {
          assertThat(serverToPlayer.getYou()).isEqualTo("GoDaddy");
          return new TestHandler() {
            @Override
            public SetupP2S setup(final SetupS2P message) {
              assertThat(receivedSetup[0]).isFalse();
              assertThat(message.getPunter()).isEqualTo(0);
              receivedSetup[0] = true;
              return new SetupP2S();
            }

            @Override
            public GameplayP2S gameplay(final GameplayS2P message) {
              assertThat(receivedSetup[0]).isTrue();
              assertThat(message.getPreviousMoves().getMoves().size()).isEqualTo(0);
              throw new DummyException();
            }
          };
        });

        throw new IllegalStateException();
      } catch (DummyException e) {
        // do nothing
      }
    }
  }

  @Test
  public void canDeserializeOfflineGameplay() throws Exception {
    try (final StateMachine sm = createStateMachine(
        "HandshakeS2P.json",
        "GameplayS2P.json")) {
      try {
        sm.handshake(new HandShakeP2S(), serverToPlayer -> {
          assertThat(serverToPlayer.getYou()).isEqualTo("GoDaddy");
          return new TestHandler() {
            @Override
            public SetupP2S setup(final SetupS2P message) {
              throw new IllegalStateException("No setup message expected for offline move messages");
            }

            @Override
            public GameplayP2S gameplay(final GameplayS2P message) {
              assertThat(message.getPreviousMoves().getMoves().size()).isEqualTo(0);
              throw new DummyException();
            }
          };
        });

        throw new IllegalStateException();
      } catch (DummyException e) {
        // do nothing
      }
    }
  }
}
