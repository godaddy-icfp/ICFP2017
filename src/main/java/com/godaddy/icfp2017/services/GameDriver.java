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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class GameDriver {

  private final InputStream inputStream;
  private final PrintStream outputStream;
  private final PrintStream debugStream;

  private final GameLogic gameLogic;

  private final InboundMessageParser inboundMessageParser;
  private final ObjectMapper mapper = new ObjectMapper();


  public GameDriver(final InputStream inputStream, final OutputStream outputStream, final OutputStream debugStream, final GameLogic gameLogic)
      throws Exception {
    this.inputStream = inputStream;

    this.outputStream = new PrintStream(outputStream);

    if (debugStream != null) {
      this.debugStream = new PrintStream(debugStream);
    }
    else {
      this.debugStream = null;
    }

    this.gameLogic = gameLogic;

    inboundMessageParser = new InboundMessageParser();


  }

  public void run() throws Exception {

    // send the handshake message to the server
    final HandShakeP2S handShakeP2S = new HandShakeP2S();
    handShakeP2S.setMe("Blah");
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
        sendMessage(setupP2S);
      }

      // gameplay
      else if (s2p instanceof GameplayS2P) {
        final GameplayS2P gameplayS2P = (GameplayS2P) s2p;
        final GameplayP2S gameplayP2S = gameLogic.move(gameplayS2P);
        sendMessage(gameplayP2S);
      }

      else if (s2p == null) {
        break;
      }
    }
  }

  public void sendMessage(P2S p2s) throws JsonProcessingException {

    String json = mapper.writeValueAsString(p2s);
    final String output = json.length() + 1 + ":" + json + '\n';
    outputStream.print(output);
    if (debugStream != null) {
      debugStream.println("out => " + output);
    }
  }

  public S2P getMessage() throws Exception {
    final int messageLength = readInteger();
    if (messageLength == 0)
      return null;

    final String messageString = readString(messageLength);

    if (debugStream != null) {
      debugStream.println("in => " + messageLength + ":" +  messageString);
    }

    if (messageString != null && messageString.length() > 0) {
      final S2P nextMessage = inboundMessageParser.getNextMessage(messageString);
      return nextMessage;
    }
    else {
      return null;
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
    while(totalLengthRead < length) {
      int charsToRead = Math.min(buffer.length, length - totalLengthRead);
      final int bytesRead = inputStream.read(buffer, 0, charsToRead);
      totalLengthRead += bytesRead;
      sb.append(new String(buffer, 0, bytesRead));
    }
    return sb.toString();
  }
}
