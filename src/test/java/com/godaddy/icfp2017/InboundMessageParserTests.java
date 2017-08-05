package com.godaddy.icfp2017;

import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.S2P;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.services.InboundMessageParser;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InboundMessageParserTests {

  @Test
  public void canDeserializeHandshakeS2P() throws Exception {
    InboundMessageParser parser = new InboundMessageParser();
    final S2P s2p = parser.getNextMessage(Resources.toString(Resources.getResource("HandshakeS2P.json"),
                                                             Charsets.UTF_8)).get();

    assertThat(s2p).isInstanceOf(HandshakeS2P.class);
    HandshakeS2P handshakeS2P = (HandshakeS2P) s2p;

    assertThat(handshakeS2P.getYou()).isEqualTo("GoDaddy");
  }

  @Test
  public void canDeserializeSetup() throws Exception {
    InboundMessageParser parser = new InboundMessageParser();
    final S2P s2p = parser.getNextMessage(Resources.toString(Resources.getResource("SetupS2P.json"), Charsets.UTF_8))
                          .get();

    assertThat(s2p).isInstanceOf(SetupS2P.class);
    SetupS2P setupS2P = (SetupS2P) s2p;

    assertThat(setupS2P.getPunter()).isEqualTo(0);
  }

  @Test
  public void canDeserializeGameplay() throws Exception {
    InboundMessageParser parser = new InboundMessageParser();
    final S2P s2p = parser.getNextMessage(Resources.toString(Resources.getResource("GameplayS2P.json"),
                                                             Charsets.UTF_8)).get();

    assertThat(s2p).isInstanceOf(GameplayS2P.class);
    GameplayS2P gameplayS2P = (GameplayS2P) s2p;

    assertThat(gameplayS2P.getPreviousMoves().getMoves().size()).isEqualTo(0);
  }
}
