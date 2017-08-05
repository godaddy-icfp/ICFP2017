package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToPathAlgorithm implements GraphAlgorithm {
  private final SimpleWeightedGraph<Site, River> graph;
  private final State state;

  public AdjacentToPathAlgorithm(final State state) {
    this.graph = state.getGraph();
    this.state = state;
  }

  public void run() {
    this.graph.edgeSet()
              .forEach(river -> {
                boolean sourceConnected = false;
                boolean targetConnected = false;

                Site source = graph.getEdgeSource(river);
                for (River sourceRiver : graph.edgesOf(source)) {
                  if (sourceRiver.getClaimedBy() == this.state.getPunter()) {
                    sourceConnected = true;
                  }
                }

                Site target = graph.getEdgeTarget(river);
                for (River targetRiver : graph.edgesOf(target)) {
                  if (targetRiver.getClaimedBy() == this.state.getPunter()) {
                    targetConnected = true;
                  }
                }

                if (sourceConnected && targetConnected) {
                  river.getAlgorithmWeights().put(Algorithms.AdjacentToPath, Weights.Decent);
                }
                else if (sourceConnected || targetConnected) {
                  river.getAlgorithmWeights().put(Algorithms.AdjacentToPath, Weights.Desired);
                }
              });
  }
}
