package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToPathAlgorithm extends BaseAlgorithm {

  public AdjacentToPathAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    graph.vertexSet().forEach(site -> {
      int count = 0;
      for (River river: graph.edgesOf(site)) {
        if (river.getClaimedBy() == state.getPunter()) {
          count++;
        }
      }
      site.setOwnClaimCount(count);
    });

    graph.edgeSet()
      .forEach(river -> {
        Site source = graph.getEdgeSource(river);
        Site target = graph.getEdgeTarget(river);
        int sourceConnectedCount = source.getOwnClaimCount();
        int targetConnectedCount = target.getOwnClaimCount();

        if (sourceConnectedCount == 0 ^ targetConnectedCount == 0) {
          double weight;
          double factor = 0.3;
          double ownedCount = sourceConnectedCount + targetConnectedCount;
          Site notOwnedSite = sourceConnectedCount == 0 ? source : target;

          weight = 1 + (ownedCount + graph.edgesOf(notOwnedSite).size() - 1) * factor;
          setter.apply(river, weight);
        }
        else
        {
          setter.apply(river, Weights.Identity);
        }
      });
  }
}
