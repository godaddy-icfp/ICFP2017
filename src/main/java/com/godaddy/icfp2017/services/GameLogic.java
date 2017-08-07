package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.*;
import com.godaddy.icfp2017.services.algorithms.Algorithms;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Optional;

public class GameLogic {

  private GameInitiator gameInitiator;

  private State previousOnlineState;
  private final PrintStream debugStream;

  public GameLogic(final PrintStream debugStream) {
    this.gameInitiator = new GameInitiator(debugStream);
    this.debugStream = debugStream;
  }

  public SetupP2S setup(final SetupS2P setup) {
    this.previousOnlineState = gameInitiator.createState(setup);
    // respond
    SetupP2S response = new SetupP2S();
    response.setReady(setup.getPunter());
    response.setState(this.previousOnlineState);
    return response;
  }

  public GameplayP2S move(final GameplayS2P move) {
    final long startTime = System.currentTimeMillis();

    final EnumSet<Algorithms> runAlgorithms = EnumSet.of(
        Algorithms.AdjacentToMine,
        Algorithms.AdjacentToPath,
        Algorithms.Connectedness,
        Algorithms.EnemyPath,
        Algorithms.MineToMine,
        Algorithms.MinimumSpanningTree);


    try (final GameAlgorithms algorithms = createGameAlgorithms(runAlgorithms)) {
      return createGameMoves(getMoveState(move), debugStream, algorithms).getMove(startTime, move);
    }
  }


  private State getMoveState(GameplayS2P move) {
    final State currentState = Optional.ofNullable(move.getPreviousState())
                                       .orElseGet(() -> Optional.ofNullable(this.previousOnlineState)
                                                                .orElseThrow(IllegalStateException::new));
    return currentState;
  }

  protected GameAlgorithms createGameAlgorithms(final EnumSet<Algorithms> selectedAlgorithms) {
    return new GameAlgorithms(debugStream, selectedAlgorithms);
  }

  protected GameMove createGameMoves(
      final State currentState,
      final PrintStream debugStream,
      final GameAlgorithms algorithms) {
    return new GameMove(currentState, debugStream, algorithms);
  }
}
