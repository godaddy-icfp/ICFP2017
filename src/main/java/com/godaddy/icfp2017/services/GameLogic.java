package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import java.io.PrintStream;

public class GameLogic {

  private GameInitiator gameInitiator;

  private State currentState;
  private final PrintStream debugStream;

  public GameLogic(final PrintStream debugStream) {
    this.gameInitiator = new GameInitiator();
    this.debugStream = debugStream;
  }

  public SetupP2S setup(final SetupS2P setup) {
    this.currentState = gameInitiator.createState(setup);
    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(this.currentState);
    return response;
  }

  public GameplayP2S move(final GameplayS2P move) {
    return new GameMove(this.currentState, this.debugStream).getMove(move);
  }
}
