package com.godaddy.icfp2017;

import com.godaddy.icfp2017.services.GameDriver;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GameLogicImpl;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.BufferedInputStream;
import java.net.Socket;

public class Application {

  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();

    parser.accepts("mode")
          .withRequiredArg().ofType(String.class)
          .defaultsTo("offline")
    .describedAs("offline or online");

    parser.accepts("host")
          .withOptionalArg().ofType(String.class)
          .describedAs("online server hostname or ip address");

    parser.accepts("port")
          .withOptionalArg().ofType(Integer.class)
          .describedAs("online server ip address");

    parser.accepts("?")
          .forHelp();

    OptionSet options = parser.parse(args);

    if (options.has("?")) {
      parser.printHelpOn(System.out);
      return;
    }

    GameLogic gameLogic = new GameLogicImpl();


    GameDriver gameDriver = null;

    if (options.valueOf("mode").equals("offline")) {
      gameDriver = new GameDriver(System.in, System.out, System.err, gameLogic);
    }

    if (options.valueOf("mode").equals("online")) {
      final String host = (String) options.valueOf("host");
      final Integer port = (Integer) options.valueOf("port");
      Socket skt = new Socket(host, port);
      gameDriver = new GameDriver(new BufferedInputStream(skt.getInputStream()), skt.getOutputStream(), System.out, gameLogic);
    }

    gameDriver.run();

  }
}
