package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.Path;
import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.base.Preconditions;

import java.util.Optional;

final public class MineToMineAlgorithm extends BaseAlgorithm {

  public MineToMineAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    super(getter, setter);
  }

  static double pathWeight(final double weight, final double length, final double optimalLength, final double diameter) {
    Preconditions.checkArgument(weight <= length);
    final double smoothedOwnership = (optimalLength - weight + 1.0) / (length + 1.0);
    final double l0 = optimalLength * optimalLength;
    final double d0 = diameter * diameter;
    final double percentage = Math.min(smoothedOwnership * (l0 / d0), 1.0);
    return Weights.Identity + (percentage * (Weights.HighlyDesired - Weights.Identity));
  }

  @Override
  public void iterate(final State state) {
    Path highestValuePath = state.getRankedPaths().first();

    // the longest shortest path in the graph
    final double diameter = highestValuePath.getLength();

    state.getMineToMinePaths().forEach(path -> {
      final int source = path.getStartVertex().getId();
      final int target = path.getEndVertex().getId();

      Optional<Path> rankedPath = state
          .getRankedPaths()
          .stream()
          .filter(minePath -> minePath.getSource() == source && minePath.getTarget() == target)
          .findFirst();

      final double optimalLength = rankedPath.isPresent() ? rankedPath.get().getLength() : path.getLength();

      final double pathWeight = pathWeight(path.getWeight(), path.getLength(), optimalLength, diameter);
      for (final River river : path.getEdgeList()) {
        alter(river, value -> pathWeight * Math.max(1.0, value));
      }
    });
  }
}
