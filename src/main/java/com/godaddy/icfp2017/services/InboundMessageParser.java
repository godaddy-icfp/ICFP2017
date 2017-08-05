package com.godaddy.icfp2017.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.S2P;
import com.godaddy.icfp2017.models.SetupS2P;

public class InboundMessageParser {

  private final JsonFactory factory = JsonMapper.Instance.getFactory();

  public S2P getNextMessage(String input) throws Exception {
    S2P s2p = null;


    final JsonParser parser = factory.createParser(input);
    if (parser.nextToken() == JsonToken.START_OBJECT) {
      switch (parser.nextFieldName()) {
        case "you":
          s2p = parser.readValueAs(HandshakeS2P.class);
          break;
        case "punter":
          s2p = parser.readValueAs(SetupS2P.class);
          break;
        case "move":
          s2p = parser.readValueAs(GameplayS2P.class);
          break;
      }
    }

    return s2p;
  }
}
