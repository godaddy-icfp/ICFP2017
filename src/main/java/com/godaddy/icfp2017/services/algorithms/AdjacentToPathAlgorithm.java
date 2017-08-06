package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
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
                int sourceConnectedCount = graph.getEdgeSource(river).getOwnClaimCount();
                int targetConnectedCount = graph.getEdgeTarget(river).getOwnClaimCount();

                if (sourceConnectedCount + targetConnectedCount == 1 &&
                    (sourceConnectedCount == 0 || targetConnectedCount == 0)){
                  setter.apply(river, Weights.HighlyDesired);
                }
              });
  }
}
