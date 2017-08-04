package com.godaddy.icfp2017;

import com.godaddy.icfp2017.services.GameDriver;
import com.godaddy.icfp2017.services.GameLogic;
import com.godaddy.icfp2017.services.GameLogicImpl;

public class Application {
  public static void main(String[] args) throws Exception {

    GameLogic gameLogic = new GameLogicImpl();

    GameDriver gameDriver = new GameDriver(System.in, System.out, gameLogic);

    gameDriver.run();

  }
}
