package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.AdjacentToMinesAlgorithm;
import com.godaddy.icfp2017.services.algorithms.AdjacentToPathAlgorithm;
import com.godaddy.icfp2017.services.algorithms.AlgorithmFactory;
import com.godaddy.icfp2017.services.algorithms.Algorithms;
import com.godaddy.icfp2017.services.algorithms.ConnectedDecisionAlgorithm;
import com.godaddy.icfp2017.services.algorithms.ConnectednessAlgorithm;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import com.godaddy.icfp2017.services.algorithms.MineToMineAlgorithm;
import com.google.common.collect.ImmutableMap;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameAlgorithms {

  private final ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators =
      ImmutableMap.<Algorithms, AlgorithmFactory>builder()
          .put(Algorithms.AdjacentToMine, AdjacentToMinesAlgorithm::new)
          .put(Algorithms.AdjacentToPath, AdjacentToPathAlgorithm::new)
          .put(Algorithms.ConnectedDecision, ConnectedDecisionAlgorithm::new)
          .put(Algorithms.Connectedness, ConnectednessAlgorithm::new)
          .put(Algorithms.MineToMine, MineToMineAlgorithm::new)
          //.put(Algorithms.ScoringAlgo, MinePathsScoreAlgorithm::new)
          //.put(Algorithms.MinimumSpanningTree, MinimumSpanningTreeAlgorithm::new)
          .build();


  private final PrintStream debugStream;

  public GameAlgorithms(PrintStream debugStream) {
    this.debugStream = debugStream;
  }

  private final Long MAX_TURN_TIME = 900L;

  public void run(State state, Long startTime) {
    final ExecutorService executorService = Executors.newFixedThreadPool(algorithmCreators.size());
    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    Long timeAvailable = MAX_TURN_TIME - (System.currentTimeMillis() - startTime);
    for (Algorithms algorithm : algorithmCreators.keySet()) {
      final GraphAlgorithm graphAlgorithm = getGraphAlgorithm(algorithm);
      executorService.submit(() -> {
        try {
          graphAlgorithm.run(algorithm, state);
        } catch (Exception e1) {
          this.debugStream.print(algorithm);
          this.debugStream.print(" Error: ");
          this.debugStream.println(e1.toString());
        } finally {
          completeLatch.countDown();
        }
      });
    }
    try {
      final boolean allCompleted = completeLatch.await(timeAvailable, TimeUnit.MILLISECONDS);
      if (!allCompleted) {
        this.debugStream.println("Some algorithms didn't finish");
      }
    } catch (InterruptedException e) {
      // ignore so we respond
    }
    executorService.shutdown();
  }

  public GraphAlgorithm getGraphAlgorithm(Algorithms algorithm) {
    final AlgorithmFactory algorithmFactory = algorithmCreators.get(algorithm);
    if (null != algorithmFactory) {
      return algorithmFactory.create(
          river -> river.getAlgorithmWeights().getOrDefault(algorithm, Weights.Identity),
          (river, score) -> {
            river.getAlgorithmWeights().put(algorithm, score);
            return score;
          });
    }

    throw new UnsupportedOperationException("No Algorithm creator defined for: " + algorithm);
  }

   public boolean isUsingAlgorithm(final Algorithms algorithm) {
    return algorithmCreators.containsKey(algorithm);
  }
}
