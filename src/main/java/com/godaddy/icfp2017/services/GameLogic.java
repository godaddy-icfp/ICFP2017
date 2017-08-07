package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.GameplayS2P;
import com.godaddy.icfp2017.models.SetupP2S;
import com.godaddy.icfp2017.models.SetupS2P;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.Algorithms;

import java.io.PrintStream;
import java.util.EnumSet;

public class GameLogic {

  private GameInitiator gameInitiator;

  private State currentState;
  private final PrintStream debugStream;

  public GameLogic(final PrintStream debugStream) {
    this.gameInitiator = new GameInitiator(debugStream);
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
    final EnumSet<Algorithms> gameAlgos = EnumSet.of(
        Algorithms.AdjacentToMine,
        Algorithms.AdjacentToPath,
        Algorithms.Connectedness,
        Algorithms.MineToMine,
        Algorithms.MinimumSpanningTree);

    try (final GameAlgorithms algorithms = new GameAlgorithms(debugStream, gameAlgos)) {
      return new GameMove(this.currentState, debugStream, algorithms).getMove(move);
    }
  }
}
