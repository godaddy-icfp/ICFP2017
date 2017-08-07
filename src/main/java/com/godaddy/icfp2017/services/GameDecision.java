package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.Claim;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.Pass;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.google.common.collect.ImmutableMap;
import java.io.PrintStream;
import java.util.Optional;

public class GameDecision {
  private final PrintStream debugStream;

  public GameDecision(PrintStream debugStream) {
    this.debugStream = debugStream;
  }

  public GameplayP2S getDecision(State state) {
    // Compute all the weight
    Optional<River> bestRiver = this.computeWeightOnGraph(state, new GameStrategy().getStrategy(state));

    //debug best river weighting
    double bestRiverWeight = state.getGraph().getEdgeWeight(bestRiver.get());
    this.debugStream.println(String.format("riverWeight: %s", bestRiverWeight));
    this.debugStream.println(
        String.format("algoWeights: %s", bestRiver.get().getAlgorithmWeights().toString()));

    // initialize the response
    GameplayP2S response = bestRiver
        .map(river -> {
          final GameplayP2S r = new GameplayP2S();
          final Claim claim = new Claim();
          claim.setPunter(state.getPunter());
          claim.setTarget(river.getTarget());
          claim.setSource(river.getSource());
          river.setClaimedBy(state.getPunter());
          r.setClaim(claim);
          return r;
        })
        .orElseGet(() -> {
          final GameplayP2S r = new GameplayP2S();
          final Pass pass = new Pass();
          pass.setPunter(state.getPunter());
          r.setPass(pass);
          return r;
        });
    response.setState(state);
    return response;
  }

  private Optional<River> computeWeightOnGraph(
      final State state,
      final ImmutableMap<Algorithms, Double> algorithmValues) {
    // Pick any river
    River bestRiver = null;
    double bestWeight = 0.0;

    for (River river : state.getGraph().edgeSet()) {
      double weight = river.getAlgorithmWeights().entrySet().stream()
          .mapToDouble(e -> e.getValue() * algorithmValues.get(e.getKey()))
          .reduce(1.0, (x, y) -> x * y);
      state.getGraph().setEdgeWeight(river, weight);
      if (!river.isClaimed() && (weight > bestWeight)) {
        bestWeight = weight;
        bestRiver = river;
      }
    }

    // sometimes we get called and don't produce a river (because they all score 0.0)
    // while we can do a better job, let's just send a pass for now to avoid crashing
    return Optional.ofNullable(bestRiver);
  }
}
