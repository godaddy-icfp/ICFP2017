package com.godaddy.icfp2017;

import com.godaddy.icfp2017.services.GameDriver;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GameLogicImpl;

import java.io.FileInputStream;

public class Application {
  public static void main(String[] args) throws Exception {

    if(args.length > 0) {
      System.setIn(new FileInputStream(args[0]));
    }

    GameLogic gameLogic = new GameLogicImpl();

    GameDriver gameDriver = new GameDriver(System.in, System.out, gameLogic);

    gameDriver.run();

  }
}
