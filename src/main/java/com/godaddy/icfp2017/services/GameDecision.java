package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.Claim;
import com.godaddy.icfp2017.models.GameplayP2S;
import com.godaddy.icfp2017.models.Pass;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;

public class GameDecision {
  private final PrintStream debugStream;

  public GameDecision(PrintStream debugStream) {
    this.debugStream = debugStream;
  }

  public GameplayP2S getDecision(State state) {
    // Compute all the weight
    Optional<River> bestRiver = this.computeWeightOnGraph(state, new GameStrategy().getStrategy(state));

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

  protected Optional<River> computeWeightOnGraph(
      final State state,
      final ImmutableMap<Algorithms, Double> algorithmValues) {
    // Pick any river
    River bestRiver = null;
    double bestScore = 0.0;

    for (River river : state.getGraph().edgeSet()) {
      double score = calculateRiverScore(algorithmValues, river);

      if (!river.isClaimed() && (score > bestScore)) {
        bestScore = score;
        bestRiver = river;
      }
    }
    debugWeights(state, bestRiver, bestScore);


    // sometimes we get called and don't produce a river (because they all score 0.0)
    // while we can do a better job, let's just send a pass for now to avoid crashing
    return Optional.ofNullable(bestRiver);
  }

  public double calculateRiverScore(ImmutableMap<Algorithms, Double> algorithmValues, River river) {
    double score = 1.0;
    for (Map.Entry<Algorithms, Double> algorithm : algorithmValues.entrySet()) {
      Double algorithmScore = river.getAlgorithmWeights().getOrDefault(algorithm.getKey(), Weights.Identity);
      Double algorithmMultiple = algorithm.getValue();
      double intermediateScore = algorithmScore * algorithmMultiple;
      score *= intermediateScore;
    }
    return score;
  }

  protected void debugWeights(final State state, final River bestRiver, final double bestScore) {
    //debug best river weighting
    //double bestRiverWeight = state.getGraph().getEdgeWeight(bestRiver);
    debugStream.println(String.format("riverWeight: %s", bestScore));
    debugStream.println(String.format("algoWeights: %s", bestRiver.getAlgorithmWeights().toString()));
    debugStream.println(String.format("mine2mine: %s", state.getMineToMinePaths().toString()));
    final ImmutableList<River> ownedRivers = state.getGraph().edgeSet()
                                                  .stream()
                                                  .filter(r -> r.getClaimedBy() == state.getPunter())
                                                  .collect(ImmutableList.toImmutableList());
    debugStream.println(String.format("owned: %s", ownedRivers.toString()));
  }
}
