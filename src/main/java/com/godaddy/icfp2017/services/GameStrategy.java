package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.google.common.collect.ImmutableMap;

public class GameStrategy {

  // These are constants that value algorithms over all rivers
  // It allows us to select which algorithms are valuable (and which are not) for this particular move
  private final ImmutableMap<Algorithms, Double> mineAcquireStrategy = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 0.8)
      .put(Algorithms.AdjacentToPath, 0.25)
      .put(Algorithms.ConnectedDecision, 0.25)
      .put(Algorithms.Connectedness, 0.25)
      .put(Algorithms.MineToMine, 1.0)
      .put(Algorithms.MinimumSpanningTree, 0.8)
      .put(Algorithms.ScoringAlgo, 0.8)
      .build();

  private final ImmutableMap<Algorithms, Double> minimumSpanningTreeStrategy = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 0.8)
      .put(Algorithms.AdjacentToPath, 0.25)
      .put(Algorithms.ConnectedDecision, 0.25)
      .put(Algorithms.Connectedness, 0.25)
      .put(Algorithms.MineToMine, 1.0)
      .put(Algorithms.MinimumSpanningTree, 0.8)
      .put(Algorithms.ScoringAlgo, 0.8)
      .build();

  private final ImmutableMap<Algorithms, Double> pathExtendStrategy = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 0.5)
      .put(Algorithms.AdjacentToPath, 0.5)
      .put(Algorithms.ConnectedDecision, 0.25)
      .put(Algorithms.Connectedness, 0.5)
      .put(Algorithms.MineToMine, 1.0)
      .put(Algorithms.MinimumSpanningTree, 1.0)
      .put(Algorithms.ScoringAlgo, 1.0)
      .build();

  private ImmutableMap<Algorithms, Double> strategyState = mineAcquireStrategy;

  public ImmutableMap<Algorithms, Double> getStrategy(State state) {
    if (!mineAdjacenciesExist(state)) {
      return mineAcquireStrategy;
    }
    return pathExtendStrategy;
  }

  private boolean mineAdjacenciesExist(State state) {
    long mineAdjacencyCount = state
        .getMines()
        .stream()
        .flatMap(mine -> state.getGraph()
            .edgesOf(mine)
            .stream()
            .filter(river -> !river.isClaimed()))
        .count();

    return mineAdjacencyCount > 0;
  }
}
