package com.godaddy.icfp2017;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.HandshakeS2P;
import com.godaddy.icfp2017.models.S2P;
import com.godaddy.icfp2017.models.SetupS2P;

import java.io.InputStream;

public class InboundMessageParser {

  private final JsonParser parser;

  public InboundMessageParser(InputStream inputStream) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    parser = factory.createParser(inputStream);
  }

  public S2P getNextMessage() throws Exception {
    S2P s2p = null;

    if (parser.nextToken() == JsonToken.START_OBJECT) {
      switch(parser.nextFieldName()) {
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
