package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import org.jgrapht.graph.SimpleWeightedGraph;

public class ConnectednessAlgorithm extends BaseAlgorithm {

  public ConnectednessAlgorithm(
      final Getter getter,
      final Setter setter) {
    super(getter, setter);
  }

  @Override
  public void iterate(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();

    graph.edgeSet()
      .forEach(river -> {
        Site source = graph.getEdgeSource(river);
        Site target = graph.getEdgeTarget(river);
        int sourceConnectedCount = source.getOwnedConnectedRivers();
        int targetConnectedCount = target.getOwnedConnectedRivers();

        if (sourceConnectedCount == 0 ^ targetConnectedCount == 0) {
          int ownedCount = sourceConnectedCount + targetConnectedCount;
          Site notOwnedSite = sourceConnectedCount == 0 ? source : target;

          int connectivity = ownedCount + notOwnedSite.getTotalConnectedRivers() - 1;
          double relativeConnectivity = connectivity / graph.edgeSet().size();
          double benefit = relativeConnectivity / 2.0;

          setter.apply(river, Weights.Identity + benefit);
        }
        else
        {
          setter.apply(river, Weights.Identity);
        }
      });
  }
}
