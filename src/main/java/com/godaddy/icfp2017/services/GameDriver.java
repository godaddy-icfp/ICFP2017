package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.icfp2017.InboundMessageParser;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandShakeP2S;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.S2P;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class GameDriver {

  private final InputStream inputStream;
  private final PrintStream outputStream;

  private final GameLogic gameLogic;
  private final ObjectMapper mapper;

  public GameDriver(final InputStream inputStream, final OutputStream outputStream, final GameLogic gameLogic) {
    this.inputStream = inputStream;
    this.outputStream = new PrintStream(outputStream);
    this.gameLogic = gameLogic;
    mapper = new ObjectMapper();

  }

  public void run() throws Exception {
    final InboundMessageParser parser = new InboundMessageParser(inputStream);

    // send the handshake message to the server
    final HandShakeP2S handShakeP2S = new HandShakeP2S();
    handShakeP2S.setMe("GoDaddy");
    outputStream.println(mapper.writeValueAsString(handShakeP2S));

    // get the handshake from the server
    final HandshakeS2P handshakeS2P = (HandshakeS2P) parser.getNextMessage();

    // read the next message from the server
    final S2P s2p = parser.getNextMessage();

    // setup
    if (s2p instanceof SetupS2P) {
      final SetupS2P setupS2P = (SetupS2P) s2p;
      final SetupP2S setupP2S = gameLogic.setup(setupS2P);
      outputStream.println(mapper.writeValueAsString(setupP2S));
    }

    // gameplay
    else if (s2p instanceof GameplayS2P) {
      final GameplayS2P gameplayS2P = (GameplayS2P) s2p;
      final GameplayP2S gameplayP2S = gameLogic.move(gameplayS2P);
      outputStream.println(mapper.writeValueAsString(gameplayP2S));
    }
  }
}
