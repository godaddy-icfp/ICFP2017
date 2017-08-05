package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToMinesAlgorithm implements GraphAlgorithm {
  private SimpleWeightedGraph<Site, River> graph;

  public AdjacentToMinesAlgorithm(final State state) {
    this.graph = state.getMap();
  }

  @Override
  public void run() {
      this.graph.edgeSet()
              .forEach(river -> {
                  if (graph.getEdgeSource(river).isMine() || graph.getEdgeTarget(river).isMine()) {
                      river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Max);
                  } else {
                      river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Identity);
                  }
              });
  }
}
