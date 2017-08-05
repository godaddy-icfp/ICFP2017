package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandShakeP2S;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.P2S;
import com.godaddy.icfp2017.models.S2P;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public class GameDriver {

  private final InputStream inputStream;
  private final PrintStream outputStream;
  private final PrintStream debugStream;

  private final GameLogic gameLogic;
  private final BufferedReader bufferedReader;
  private final InboundMessageParser inboundMessageParser;
  private final ObjectMapper mapper = new ObjectMapper();


  public GameDriver(final InputStream inputStream, final OutputStream outputStream, final GameLogic gameLogic)
      throws Exception {
    this.inputStream = inputStream;

    this.outputStream = new PrintStream(outputStream);
    this.debugStream = new PrintStream(System.out);

    this.gameLogic = gameLogic;

    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

    inboundMessageParser = new InboundMessageParser();

  }

  public void run() throws Exception {

    // send the handshake message to the server
    final HandShakeP2S handShakeP2S = new HandShakeP2S();
    handShakeP2S.setMe("GoDaddy");
    sendMessage(handShakeP2S);

    // get the handshake from the server
    final HandshakeS2P handshakeS2P = (HandshakeS2P) getMessage();

    while(true) {
      // read the next message from the server
      final S2P s2p = getMessage();

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

  private void sendMessage(P2S p2s) throws JsonProcessingException {
    String json = mapper.writeValueAsString(p2s);
    final String output = json.length() + ":" + json;
    outputStream.println(output);
    if (debugStream != null) {
      debugStream.println(output);
    }

  }

  private S2P getMessage() throws Exception {
    while (!bufferedReader.ready()) {
      Thread.sleep(10);
    }

    String response = bufferedReader.readLine();
    if (debugStream != null) {
      debugStream.println(response);
    }

    if (response != null && response.length() > 0) {
      final String jsonResponse = response.substring(response.indexOf(':') + 1);
      final S2P nextMessage = inboundMessageParser.getNextMessage(jsonResponse);
      return nextMessage;
    }
    else {
      return null;
    }
  }
}
