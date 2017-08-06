package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.List;

public final class RiverWeightingAlgorithm extends BaseAlgorithm {
  public RiverWeightingAlgorithm(final Getter getter, final Setter setter) {
    super(getter, setter);
  }

  private double assignInitialWeights(final State state) {
    final ImmutableSet<Site> mines = state.getMines();
    final FloydWarshallShortestPaths<Site, River> shortestPaths = state.getShortestPaths();
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    final DepthFirstIterator<Site, River> iter = new DepthFirstIterator<>(graph);
    double maxWeight = 0.0;
    while (iter.hasNext()) {
      final Site site = iter.next();
      for (final Site mine : mines) {
        final GraphPath<Site, River> path = shortestPaths.getPath(mine, site);
        final List<River> edgeList = path.getEdgeList();
        for (int i = 0; i < edgeList.size(); i++) {
          final double i2 = (i + 1) * (i + 1);
          final River river = edgeList.get(i);
          maxWeight = Math.max(maxWeight, alter(river, value -> value + i2));
        }
      }
    }

    return maxWeight;
  }

  private void adjustWeights(final State state, final double maxWeight) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    for (final River river : graph.edgeSet()) {
      alter(river, value -> Math.min(Weights.Max, 1.0 + (value / maxWeight)));
    }
  }

  @Override
  public void iterate(final State state) {
    final double maxWeight = assignInitialWeights(state);
    adjustWeights(state, maxWeight);
  }
}
