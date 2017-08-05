package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToPathAlgorithm implements GraphAlgorithm {
  private final Setter setter;

  public AdjacentToPathAlgorithm(
      final GraphAlgorithm.Getter getter,
      final GraphAlgorithm.Setter setter) {
    this.setter = setter;
  }

  @Override
  public void run(final State state) {
    final SimpleWeightedGraph<Site, River> graph = state.getGraph();
    graph.edgeSet()
              .forEach(river -> {
                boolean sourceConnected = false;
                boolean targetConnected = false;

                Site source = graph.getEdgeSource(river);
                for (River sourceRiver : graph.edgesOf(source)) {
                  if (sourceRiver.getClaimedBy() == state.getPunter()) {
                    sourceConnected = true;
                  }
                }

                Site target = graph.getEdgeTarget(river);
                for (River targetRiver : graph.edgesOf(target)) {
                  if (targetRiver.getClaimedBy() == state.getPunter()) {
                    targetConnected = true;
                  }
                }

                if (sourceConnected ^ targetConnected) {
                  setter.apply(river, Weights.Desired);
                }
              });
  }
}
