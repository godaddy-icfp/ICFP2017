package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
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
      .put(Algorithms.ScoringAlgo, 0.6)
      .put(Algorithms.EnemyPath, 0.25)
      .build();

  private final ImmutableMap<Algorithms, Double> minimumSpanningTreeStrategy = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 0.25)
      .put(Algorithms.AdjacentToPath, 0.25)
      .put(Algorithms.ConnectedDecision, 0.25)
      .put(Algorithms.Connectedness, 0.25)
      .put(Algorithms.MineToMine, 1.0)
      .put(Algorithms.MinimumSpanningTree, 1.0)
      .put(Algorithms.ScoringAlgo, 0.8)
      .put(Algorithms.EnemyPath, 0.25)
      .build();

  private final ImmutableMap<Algorithms, Double> pathExtendStrategy = ImmutableMap.<Algorithms, Double>builder()
      .put(Algorithms.AdjacentToMine, 0.5)
      .put(Algorithms.AdjacentToPath, 0.5)
      .put(Algorithms.ConnectedDecision, 0.25)
      .put(Algorithms.Connectedness, 0.5)
      .put(Algorithms.MineToMine, 1.0)
      .put(Algorithms.MinimumSpanningTree, 1.0)
      .put(Algorithms.ScoringAlgo, 0.8)
      .put(Algorithms.EnemyPath, 0.8)
      .build();

  public ImmutableMap<Algorithms, Double> getStrategy(State state) {
    if (mstFinished(state)) {
      return pathExtendStrategy;
    }
    if (minesConnected(state)) {
      return mineAcquireStrategy;
    }

    return minimumSpanningTreeStrategy;
  }

  private boolean minesConnected(final State state) {
    if (state.getTotalClaimed() > state.getGraph().edgeSet().size() / 10) {
      // Trying too long...
      return true;
    }
    for (Site site: state.getMines()) {
      boolean isAvailable = false;
      for (River river: state.getGraph().edgesOf(site)) {
        if (!river.isClaimed() && !site.isOwned()) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean mstFinished(final State state) {
    boolean notFinished = state.getGraph().edgeSet()
        .stream()
        .filter(river -> !river.isClaimed())
        .anyMatch(river -> river.getAlgorithmWeights().get(Algorithms.MinimumSpanningTree) != null);

    return !notFinished;
  }
}
