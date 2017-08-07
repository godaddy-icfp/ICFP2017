package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.algorithms.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameAlgorithms implements AutoCloseable {

  private static final ImmutableMap<Algorithms, AlgorithmFactory> AllAlgorithmCreators =
      ImmutableMap.<Algorithms, AlgorithmFactory>builder()
          .put(Algorithms.AdjacentToMine, AdjacentToMinesAlgorithm::new)
          .put(Algorithms.AdjacentToPath, AdjacentToPathAlgorithm::new)
          .put(Algorithms.ConnectedDecision, ConnectedDecisionAlgorithm::new)
          .put(Algorithms.Connectedness, ConnectednessAlgorithm::new)
          .put(Algorithms.MineToMine, MineToMineAlgorithm::new)
          .put(Algorithms.ScoringAlgo, MinePathsScoreAlgorithm::new)
          .put(Algorithms.MinimumSpanningTree, MinimumSpanningTreeAlgorithm::new)
          .put(Algorithms.EnemyPath, EnemyPathWeightAlgorithm::new)
          .put(Algorithms.PathExtension, PathExtensionAlgorithm::new)
          .put(Algorithms.FullMST, FullMSTAlgorithm::new)
          .build();

  private static final EnumSet<Algorithms> DefaultAlgorithms =
      EnumSet.complementOf(
          EnumSet.of(Algorithms.MinimumSpanningTree,
                     Algorithms.ScoringAlgo));

  private final ImmutableMap<Algorithms, AlgorithmFactory> algorithmCreators;
  ;

  private final PrintStream debugStream;
  final ExecutorService executorService;

  public GameAlgorithms(final PrintStream debugStream) {
    this(debugStream, DefaultAlgorithms);
  }

  public GameAlgorithms(
      final PrintStream debugStream,
      final Set<Algorithms> algorithmsToUse) {
    this.debugStream = debugStream;
    algorithmCreators = ImmutableMap.copyOf(Maps.filterKeys(AllAlgorithmCreators, algorithmsToUse::contains));
    executorService = Executors.newFixedThreadPool(algorithmCreators.size());
  }

  private final Long MAX_TURN_TIME = 900L;

  public void run(State state, Long startTime) {
    final CountDownLatch completeLatch = new CountDownLatch(algorithmCreators.size());

    long timeAvailable = MAX_TURN_TIME - (System.currentTimeMillis() - startTime);
    for (Algorithms algorithm : algorithmCreators.keySet()) {
      final GraphAlgorithm graphAlgorithm = getGraphAlgorithm(algorithm);
      executorService.submit(() -> {
        try {
          graphAlgorithm.run(algorithm, state);
        }
        catch (Exception e1) {
          this.debugStream.print(algorithm);
          this.debugStream.print(" Error: ");
          e1.printStackTrace(this.debugStream);
        }
        finally {
          completeLatch.countDown();
        }
      });
    }
    try {
      final boolean allCompleted = completeLatch.await(timeAvailable, TimeUnit.MILLISECONDS);
      if (!allCompleted) {
        debugStream.println("Some algorithms didn't finish");
      }
      debugStream.println("had " + timeAvailable + " time available");
    }
    catch (InterruptedException e) {
      // ignore so we respond
    }
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

  @Override
  public void close() {
    executorService.shutdown();
  }
}
