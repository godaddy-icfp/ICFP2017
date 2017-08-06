package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.Path;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.base.Preconditions;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;

final public class MineToMineAlgorithm extends BaseAlgorithm {

  public MineToMineAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    super(getter, setter);
  }

  static double pathWeight(final double weight, final double length, final double diameter) {
    Preconditions.checkArgument(weight <= length);
    final double smoothedOwnership = (length - weight + 1.0) / (length + 1.0);
    final double l0 = length * length;
    final double d0 = diameter * diameter;
    final double percentage = Math.min(smoothedOwnership * (l0 / d0), 1.0);
    return 1.0 + (percentage * (Weights.HighlyDesired - 1.0));
  }

  @Override
  public void iterate(final State state) {
    final FloydWarshallShortestPaths<Site, River> shortestPaths = state.getShortestPaths();

    Path highestValuePath = state.getRankedPaths().first();

    // the longest shortest path in the graph
    final double diameter = highestValuePath.getLength();

    state.getMineToMinePaths().forEach(path -> {
      final double pathWeight = pathWeight(path.getWeight(), path.getLength(), diameter);
      for (final River river : path.getEdgeList()) {
        alter(river, value -> pathWeight * Math.max(1.0, value));
      }
    });
  }
}
